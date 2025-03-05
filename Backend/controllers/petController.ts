import { Request, Response } from "express";
import mongoose, { Types } from "mongoose";
import Pet, { IPet } from "../models/Pet";
import Household from "../models/Household";

// Add a new pet
export const addPet = async (req: Request, res: Response): Promise<void> => {
    try {
        const { name, householdId, feedingTime } = req.body;
        console.log("adding pet with name:%s, house:%s, time:%s", name, householdId, feedingTime)
        

        let household = null;
        if (householdId) {
            household = await Household.findById(householdId);
            if (!household) {
                res.status(404).json({ message: "Household not found" });
                return;
            }
        }

        const pet = new Pet({
            name,
            householdId: householdId,
            feedingTime: new Date(feedingTime),
            fed: false
        });

        await pet.save();

        /*if (household) {
            household.pets.push(pet._id as Types.ObjectId);
            await household.save();
        }*/

        res.status(201).json({ message: "Pet added successfully", pet });
    } catch (error) {
        console.log(error)
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

        if (fed === undefined) {
            res.status(400).json({ message: "'fed' field is required" });
            return;
        }

        if (typeof fed !== "boolean") {
            res.status(400).json({ message: "'fed' must be a boolean" });
            return;
        }

        const updateFields: Partial<IPet> = { fed };
        if (fed === true) {
            updateFields.lastTimeFed = new Date();
        }

        const pet = await Pet.findByIdAndUpdate(petId, updateFields, { new: true });

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

        const pet = await Pet.findByIdAndDelete(petId);
        if (!pet) {
            res.status(404).json({ message: "Pet not found" });
            return;
        }

        res.json({ message: "Pet removed successfully" });
    } catch (error) {
        res.status(500).json({ message: "Error removing pet", error });
    }
};