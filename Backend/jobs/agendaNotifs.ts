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

        // Fetch every user in that household
        const users = await User.find({ householdId: pet.householdId });
        if (users.length === 0) {
          console.warn(`No users found for household ${pet.householdId}`);
          continue;
        }

        for (const user of users) {
          const token = user.FCMToken;
          if (!token) {
            console.warn(`No FCM token for user ${user.email}`);
            continue;
          }
        
          const message = {
            notification: {
              title: "Feeding Reminder",
              body: `It's time to feed your pet: ${pet.name}`,
            },
            token,
          };
        
          try {
            const response = await admin.messaging().send(message);
            console.log(`Notification sent to ${user.email} for pet ${pet.name}: ${response}`);
          } catch (err) {
            console.error(`Error sending notification to ${user.email} for pet ${pet.name}:`, err);
          }
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