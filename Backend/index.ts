import express from "express";
import session from "express-session";
import passport from "passport";
import connectDB from "./config/db";
import authRoutes from "./routes/authRoutes";
import userHouseholdRoutes from "./routes/userHouseholdRoutes";
import logRoutes from "./routes/logRoutes";
import analyticsRoutes from "./routes/analyticsRoutes";
import userRoutes from "./routes/userRoutes";
import petRoutes from "./routes/petRoutes";

const app = express();

// Connect to database
connectDB();

// Middleware
app.use(express.json());
app.use(session({ secret: "supersecret", resave: false, saveUninitialized: true }));
app.use(passport.initialize());
app.use(passport.session());

// Routes
app.use("/auth", authRoutes);
app.use("/household", userHouseholdRoutes);
app.use("/log", logRoutes);
app.use("/analytics", analyticsRoutes);
app.use("/user", userRoutes);
app.use("/pet", petRoutes);
 
const PORT = process.env.PORT || 5001;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
