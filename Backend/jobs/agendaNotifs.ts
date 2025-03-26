import { Job } from "agenda";
import Pet from "../models/Pet";
import Household from "../models/Household";
import User from "../models/User";
import admin from "../config/firebase";
const Agenda = require("agenda");

const agendaNotifs = new Agenda({
  db: { address: `${process.env.DB_URI}/pet-tracker` }
});

// Define the feeding notification job
agendaNotifs.define("send feeding notifications", async (job: Job) => {
  console.log(`Job ${job.attrs.name} executing at ${new Date()}`);
  const now = new Date();
  const currentHour = now.getHours();
  const currentMinute = now.getMinutes();

  try {
    // Fetch all pets (consider optimizing this query in production)
    const pets = await Pet.find({});
    for (const pet of pets) {
      // Convert the stored feedingTime (e.g., "1970-01-01T14:30:00.000Z") to a Date
      const petFeedingTime = new Date(pet.feedingTime);
      if (
        petFeedingTime.getHours() === currentHour &&
        petFeedingTime.getMinutes() === currentMinute && 
        !pet.fed
      ) {
        // Look up the household to get the owner's email
        if (!pet.householdId) continue;
        const household = await Household.findById(pet.householdId);
        if (!household || !household.managerId) {
          console.warn(`Household or owner email not found for pet ${pet.name}`);
          continue;
        }
        // Get the user's device token using the owner's email
        const user = await User.findById({ email: household.managerId });
        const token = user?.FCMToken;
        if (!token) {
          console.warn(`FCM token not found for user ${household.managerId}`);
          continue;
        }
        // Build and send the notification message
        const message = {
          notification: {
            title: "Feeding Reminder",
            body: `It's time to feed your pet: ${pet.name}`,
          },
          token: token
        };
        try {
          const response = await admin.messaging().send(message);
          console.log(`Notification sent for pet ${pet.name}: ${response}`);
        } catch (err) {
          console.error(`Error sending notification for pet ${pet.name}:`, err);
        }
      }
    }
  } catch (error) {
    console.error("Error in feeding notifications job:", error);
  }
});

// Initialize and schedule the job
(async function initializeAgenda() {
  try {
    await agendaNotifs.start();
    console.log("Agenda started for feeding notifications");

    // Cancel any existing instances of the job to prevent duplicates
    await agendaNotifs.cancel({ name: "send feeding notifications" });
    
    // Schedule the job to run every minute
    agendaNotifs.every("1 minute", "send feeding notifications");

    console.log("Feeding notifications job scheduled to run every minute");
  } catch (err) {
    console.error("Failed to start or schedule Agenda for feeding notifications", err);
  }
})();

export default agendaNotifs;