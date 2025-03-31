import cron from "node-cron";
import Pet from "../models/Pet";
import Household from "../models/Household";
import User from "../models/User";
import admin from "../config/firebase";
import moment from "moment-timezone";

// Schedule job to run every hour
cron.schedule(
  "0 * * * *",
  async () => {
    console.log(`Feeding notifications job executing at ${moment().tz("America/Vancouver").format()}`);
    const now = moment().tz("America/Vancouver");
    const currentHour = now.hour();

    try {
      const pets = await Pet.find({});
      for (const pet of pets) {
        const petFeedingTime = new Date(pet.feedingTime);
        if (petFeedingTime.getHours() <= currentHour && !pet.fed) {
          if (!pet.householdId) continue;
          const household = await Household.findById(pet.householdId);
          if (!household || !household.managerId) {
            console.warn(`Household or owner email not found for pet ${pet.name}`);
            continue;
          }

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
  },
  {
    timezone: "America/Vancouver",
  }
);
