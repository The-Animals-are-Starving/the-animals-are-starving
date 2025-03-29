import { Job } from "agenda";
import Pet from "../models/Pet";
const Agenda = require("agenda");

const agenda = new Agenda({
  db: { address: `${process.env.DB_URI}/pet-tracker` }
});

// Define the job
agenda.define("reset pet fed status", async (job: Job) => {
    console.log(`Job ${job.attrs.name} executing at ${new Date()}`);
    try {
      const result = await Pet.updateMany({}, { $set: { fed: false } });
      console.log("Pets fed status reset:", result);
    } catch (error) {
      console.error("Error resetting pet fed status:", error);
    }
  });

  (async function initializeAgenda() {
    try {
      await agenda.start();
      console.log("Agenda started");
  
      // Cancel any existing instances of the job
      await agenda.cancel({ name: "reset pet fed status" });
      
      const job = agenda.create("reset pet fed status");
      job.repeatEvery("0 0 * * *", { timezone: "America/Vancouver", skipImmediate: true });
      await job.save();
  
      console.log("Agenda job 'reset pet fed status' scheduled to run at midnight (America/Vancouver)");
    } catch (err) {
      console.error("Failed to start Agenda", err);
    }
  })();  

export default agenda;