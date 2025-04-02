import { Request, Response } from "express";
import User, { UserRole } from "../models/User";

// Create a new user (defaults to "normal" role)
export const createUser = async (req: Request, res: Response): Promise<void> => {
    try {
        const { email, name, householdId } = req.body;

        // Check if user already exists
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            res.status(400).json({ message: "User already exists" });
            return;
        }

        // Create new user with default role "normal"
        const user = new User({ 
            email, 
            name, 
            householdId, role: "normal" });
        await user.save();

        res.status(201).json({ message: "User created successfully", user });
    } catch (error) {
        res.status(500).json({ message: "Error creating user", error });
    }
};

// Get all users
export const getAllUsers = async (req: Request, res: Response): Promise<void> => {
    try {
        const { householdId } = req.params;
        const users = await User.find({householdId: householdId}).sort({ name: 1 });
        console.log(users);
        res.json(users);
    } catch (error) {
        res.status(500).json({ message: "Error retrieving users", error });
    }
};

// Get user details by email
export const getUser = async (req: Request, res: Response): Promise<void> => {
    try {
        const { email } = req.params;
        const user = await User.findOne({ email });

        if (!user) {
            res.status(404).json({ message: "User not found" });
            return;
        }

        res.json(user);
    } catch (error) {
        res.status(500).json({ message: "Error retrieving user", error });
    }
};

// Update user details (including role)
export const updateUserRole = async (req: Request, res: Response): Promise<void> => {
    try {
        const { email } = req.params;
        const updates = req.body;

        // Validate role if updating it
        const validRoles: UserRole[] = ["restricted", "normal", "manager"];
        if (updates.role && !validRoles.includes(updates.role)) {
            res.status(400).json({ message: "Invalid role provided" });
            return;
        }

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

// Update user details (including role)
export const updateUserHouseholdId = async (req: Request, res: Response): Promise<void> => {
    try {
        const { email } = req.params;
        const updates = req.body;

        const user = await User.findOneAndUpdate({ email }, updates, { new: true });

        if (!user) {
            res.status(404).json({ message: "User not found" });
            return;
        }

        res.json({ message: "User Household ID updated successfully", user });
    } catch (error) {
        res.status(500).json({ message: "Error updating user household ID", error });
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

        res.json(true);
    } catch (error) {
        res.status(500).json({ message: "Error deleting user", error });
    }
};