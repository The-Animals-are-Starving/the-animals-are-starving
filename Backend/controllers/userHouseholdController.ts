import { Request, Response, NextFunction } from "express";
import Household from "../models/Household";
import User from "../models/User";
import { Types } from "mongoose";

// Create a new household
export const createHousehold = async (req: Request, res: Response): Promise<void> => {
    try {
        const { householdName, managerEmail } = req.body;

        // Validate manager user
        const manager = await User.findOne({ email: managerEmail });
        if (!manager) {
            res.status(404).json({ message: "Managing user not found" });
            return;
        }

        // Create household
        const household = new Household({
            name: householdName,
            managerId: manager._id,
            users: [manager._id], 
            pets: [],
        });

        await household.save();
        res.status(201).json(household);
    } catch (error) {
        console.error("Error:", error);
        res.status(500).json({ message: "An internal server error occurred" });
    }
};

// Add a user to a household using email
export const addUserToHousehold = async (req: Request, res: Response): Promise<void> => {
    try {
        const { householdId, email } = req.body;

        // Validate household
        const household = await Household.findById(householdId);
        if (!household) {
            res.status(404).json({ message: "Household not found" });
            return;
        }

        // Find user by email
        const user = await User.findOne({ email });
        if (!user) {
            res.status(404).json({ message: "User not found" });
            return;
        }

        // Check if user is already in the household
        if (household.users.includes(user._id as Types.ObjectId)) {
            res.status(400).json({ message: "User already in household" });
            return;
        }

        // Add user
        household.users.push(user._id as Types.ObjectId);
        await household.save();
        res.json({ message: "User added successfully", household });
    } catch (error) {
        res.status(500).json({ message: "Error adding user", error });
    }
};

// Remove a user from a household using email
export const removeUserFromHousehold = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
        const { householdId, email } = req.body;

        // Validate household
        const household = await Household.findById(householdId);
        if (!household) {
            res.status(404).json({ message: "Household not found" });
            return;
        }

        // Find user by email
        const user = await User.findOne({ email });
        if (!user) {
            res.status(404).json({ message: "User not found" });
            return;
        }

        // Check if user is in household
        if (!household.users.includes(user._id as Types.ObjectId)) {
            res.status(400).json({ message: "User not found in household" });
            return;
        }

        // Remove user
        household.users = household.users.filter((id) => !id.equals(user._id as Types.ObjectId));
        await household.save();
        res.json({ message: "User removed successfully", household });
    } catch (error) {
        res.status(500).json({ message: "Error removing user", error });
    }
};
