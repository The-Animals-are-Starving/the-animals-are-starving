import express from "express";
import "./jobs/resetFedStatusCron";
import "./jobs/passiveNotifsCron";
import connectDB from "./config/db";
import userHouseholdRoutes from "./routes/userHouseholdRoutes";
import logRoutes from "./routes/logRoutes";
import analyticsRoutes from "./routes/analyticsRoutes";
import userRoutes from "./routes/userRoutes";
import petRoutes from "./routes/petRoutes";
import notificationRoutes from "./routes/notificationRoutes";

const app = express();

// Middleware
app.use(express.json());

// Routes
app.use("/household", userHouseholdRoutes);
app.use("/log", logRoutes);
app.use("/analytics", analyticsRoutes);
app.use("/user", userRoutes);
app.use("/pet", petRoutes);
app.use("/notify", notificationRoutes);

const PORT = process.env.PORT || 5001;

connectDB()
  .then(() => {
    app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
  })
  .catch((err) => {
    console.error("Failed to connect to the database", err);
  });
