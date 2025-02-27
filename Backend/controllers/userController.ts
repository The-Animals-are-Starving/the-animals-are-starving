import { Request, Response } from "express";
import User from "../models/User";

// Create a new user
export const createUser = async (req: Request, res: Response): Promise<void> => {
    try {
        const { email, name } = req.body;

        // Check if user already exists
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            res.status(400).json({ message: "User already exists" });
            return
        }

        // Create new user
        const user = new User({ email, name });
        await user.save();

        res.status(201).json({ message: "User created successfully", user });
    } catch (error) {
        res.status(500).json({ message: "Error creating user", error });
    }
};

// Get user details by email
export const getUser = async (req: Request, res: Response): Promise<void> => {
    try {
        const { email } = req.params;
        const user = await User.findOne({ email });

        if (!user) {
            res.status(404).json({ message: "User not found" });
            return
        }

        res.json(user);
    } catch (error) {
        res.status(500).json({ message: "Error retrieving user", error });
    }
};

// Update user details
export const updateUser = async (req: Request, res: Response): Promise<void> => {
    try {
        const { email } = req.params;
        const updates = req.body;

        const user = await User.findOneAndUpdate({ email }, updates, { new: true });

        if (!user) {
            res.status(404).json({ message: "User not found" });
            return;
        }

        res.json({ message: "User updated successfully", user });
    } catch (error) {
        res.status(500).json({ message: "Error updating user", error });
    }
};

// Delete a user
export const deleteUser = async (req: Request, res: Response): Promise<void> => {
    try {
        const { email } = req.params;

        const user = await User.findOneAndDelete({ email });

        if (!user) {
            res.status(404).json({ message: "User not found" });
            return;
        }

        res.json({ message: "User deleted successfully" });
    } catch (error) {
        res.status(500).json({ message: "Error deleting user", error });
    }
};