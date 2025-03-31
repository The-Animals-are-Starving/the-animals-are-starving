import cron from "node-cron";
import Pet from "../models/Pet";

// Schedule job to run at midnight (America/Vancouver)
cron.schedule(
  "0 0 * * *",
  async () => {
    console.log(`Reset pet fed status job executing at ${new Date()}`);
    try {
      const result = await Pet.updateMany({}, { $set: { fed: false } });
      console.log("Pets fed status reset:", result);
    } catch (error) {
      console.error("Error resetting pet fed status:", error);
    }
  },
  {
    timezone: "America/Vancouver",
  }
);