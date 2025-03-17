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

        const savedHouse = await household.save();
        res.status(201).json(savedHouse);
    } catch (error) {
        console.error("Error:", error);
        res.status(500).json({ message: "An internal server error occurred" });
    }
};

