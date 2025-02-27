import { Request, Response } from "express";
import mongoose from "mongoose";
import Pet from "../models/Pet";
import Household from "../models/Household";

// Add a new pet
export const addPet = async (req: Request, res: Response): Promise<void> => {
    try {
        const { petId, name, householdId, feedingTime } = req.body;

        // Validate household if provided
        if (householdId) {
            const household = await Household.findById(householdId);
            if (!household) {
                res.status(404).json({ message: "Household not found" });
                return;
            }
        }

        // Check if petId is unique
        const existingPet = await Pet.findOne({ petId });
        if (existingPet) {
            res.status(400).json({ message: "Pet ID already exists" });
            return;
        }

        // Create new pet
        const pet = new Pet({
            petId,
            name,
            householdId: householdId ? new mongoose.Types.ObjectId(householdId) : undefined,
            feedingTime,
            fed: false
        });

        await pet.save();
        res.status(201).json({ message: "Pet added successfully", pet });
    } catch (error) {
        res.status(500).json({ message: "Error adding pet", error });
    }
};

// Get all pets in a household
export const getPetsByHousehold = async (req: Request, res: Response) => {
    try {
        const { householdId } = req.params;
        const pets = await Pet.find({ householdId }).sort({ name: 1 });

        res.json(pets);
    } catch (error) {
        res.status(500).json({ message: "Error retrieving pets", error });
    }
};

// Update pet's feeding status
export const updatePetFeedingStatus = async (req: Request, res: Response): Promise<void> => {
    try {
        const { petId } = req.params;
        const { fed } = req.body;

        const pet = await Pet.findOneAndUpdate({ petId }, { fed }, { new: true });
        if (!pet) {
            res.status(404).json({ message: "Pet not found" });
            return;
        }

        res.json({ message: "Pet feeding status updated", pet });
    } catch (error) {
        res.status(500).json({ message: "Error updating pet feeding status", error });
    }
};

// Remove a pet
export const removePet = async (req: Request, res: Response): Promise<void> => {
    try {
        const { petId } = req.params;

        const pet = await Pet.findOneAndDelete({ petId });
        if (!pet) {
            res.status(404).json({ message: "Pet not found" });
            return;
        }

        res.json({ message: "Pet removed successfully" });
    } catch (error) {
        res.status(500).json({ message: "Error removing pet", error });
    }
};