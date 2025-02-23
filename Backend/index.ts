import express from "express";
import session from "express-session";
import passport from "passport";
import connectDB from "./config/db";
import authRoutes from "./routes/authRoutes";
import userHouseholdRoutes from "./routes/userHouseholdRoutes";

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

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
