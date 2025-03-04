import { Request, Response } from "express";
import mongoose from "mongoose";
import Log from "../models/Log";
import Pet from "../models/Pet";
import User from "../models/User";
import Household from "../models/Household";

// Log a feeding event
export const logFeeding = async (req: Request, res: Response): Promise<void> => {
    try {
        const { petId, userEmail, amount, householdId } = req.body;

        // Validate user
        const user = await User.findOne({ email: userEmail });
        if (!user) {
            res.status(404).json({ message: "User not found" });
            return;
        }

        // Validate pet
        const pet = await Pet.findById(petId);
        if (!pet) {
            res.status(404).json({ message: "Pet not found" });
            return;
        }

        // Validate household if provided
        if (householdId) {
            const household = await Household.findById(householdId);
            if (!household) {
                res.status(404).json({ message: "Household not found" });
                return;
            }
        }

        // Create log entry
        const log = new Log({
            householdId: householdId ? new mongoose.Types.ObjectId(householdId) : undefined,
            petId,
            userId: user._id,
            timestamp: new Date(),
        });

        await log.save();
        res.status(201).json({ message: "Feeding logged successfully", log });
    } catch (error) {
        res.status(500).json({ message: "Error logging feeding", error });
    }
};

// Get feeding history for a pet
export const getPetFeedingHistory = async (req: Request, res: Response) => {
    try {
        const { petId } = req.params;
        const logs = await Log.find({ petId }).populate("userId", "email name").sort({ timestamp: -1 });

        res.json(logs);
    } catch (error) {
        res.status(500).json({ message: "Error retrieving feeding history", error });
    }
};

// Get feeding history for a household
export const getHouseholdFeedingHistory = async (req: Request, res: Response) => {
    try {
        const { householdId } = req.params;
        const logs = await Log.find({ householdId }).sort({ timestamp: -1 });

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

        const logs = await Log.find({ userId: user._id }).populate("petId", "name").sort({ timestamp: -1 });

        res.json(logs);
    } catch (error) {
        res.status(500).json({ message: "Error retrieving user feeding history", error });
    }
};
