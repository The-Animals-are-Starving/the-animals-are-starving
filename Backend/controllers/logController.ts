import { Request, Response } from "express";
import mongoose from "mongoose";
import Log from "../models/Log";
import Pet from "../models/Pet";
import User from "../models/User";
import Household from "../models/Household";

// Log a feeding event
export const logFeeding = async (req: Request, res: Response): Promise<void> => {
    try {
        const { petName } = req.params;
        const {userEmail, householdId, feedingAmount } = req.body;

        var amount = parseInt(feedingAmount)
        if (isNaN(amount)) {
            amount = 1;
        }

        // Validate user
        console.log("Searching for user with email, %s", userEmail);
        const user = await User.findOne({ email: userEmail });
        if (!user) {
            console.log("User not found")
            res.status(404).json({ message: "User not found" });
            return;
        }

        // Validate pet
        const pet = await Pet.findOne({name: petName});
        if (!pet) {
            console.log("Pet not found")
            res.status(404).json({ message: "Pet not found" });
            return;
        }

        // Validate household if provided
        console.log("Searching for household, %s", householdId)
        if (householdId) {
            const household = await Household.findById(householdId);
            if (!household) {
                res.status(404).json({ message: "Household not found" });
                return;
            }
            console.log("Found household, %s", household);
        }
        
        // Create log entry
        const log = new Log({
            householdId: householdId,
            petName: petName,
            userName: user.name,
            timestamp: new Date(),
            amount: amount
        });

        await log.save();
        res.status(201).json({ message: "Feeding logged successfully", log });
    } catch (error) {
        console.log(error);
        res.status(500).json({ message: "Error logging feeding", error });
    }
};

// Get feeding history for a pet
export const getPetFeedingHistory = async (req: Request, res: Response) => {
    try {
        const { petName } = req.params;
        const logs = await Log.find({ petName }).sort({ timestamp: -1 });

        res.json(logs);
    } catch (error) {
        console.log(error)
        res.status(500).json({ message: "Error retrieving feeding history", error });
    }
};

// Get feeding history for a household
export const getHouseholdFeedingHistory = async (req: Request, res: Response) => {
    try {
        const { householdId } = req.params;
        console.log("Fetching logs from household %s", householdId)
        const logs = await Log.find({ householdId }).sort({ timestamp: -1 });
        console.log(logs)
        res.json(logs);
    } catch (error) {
        res.status(500).json({ message: "Error retrieving household feeding history", error });
    }
};

// Get all feedings by a specific user
export const getUserFeedingHistory = async (req: Request, res: Response): Promise<void> => {
    try {
        const { userEmail } = req.params;
        const user = await User.findOne({ email: userEmail });
        if (!user) {
            res.status(404).json({ message: "User not found" });
            return;
        }

        const logs = await Log.find({ userName: user.name }).sort({ timestamp: -1 });

        res.json(logs);
    } catch (error) {
        res.status(500).json({ message: "Error retrieving user feeding history", error });
    }
};
