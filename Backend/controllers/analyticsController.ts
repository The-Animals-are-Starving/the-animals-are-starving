import { Request, Response } from "express";
import Log from "../models/Log";
import { Types } from "mongoose";

// Get user rankings based on feeding contributions
export const getUserRankings = async (req: Request, res: Response) => {
    try {
        const { householdId } = req.params;

        // Aggregate logs to count feedings per user
        const rankings = await Log.aggregate([
            { $match: { householdId: new Types.ObjectId(householdId) } },
            { $group: { _id: "$userId", feedCount: { $sum: 1 } } },
            { $sort: { feedCount: -1 } },
            { $lookup: { from: "users", localField: "_id", foreignField: "_id", as: "user" } },
            { $unwind: "$user" },
            { $project: { _id: 0, userId: "$user._id", name: "$user.name", feedCount: 1 } }
        ]);

        res.json(rankings);
    } catch (error: unknown) {
        console.error("Error:", error);
        res.status(500).json({ message: "An internal server error occurred" });
    }
};

// Predict feeding cost for the next month
export const predictFeedingCost = async (req: Request, res: Response): Promise<void> => {
    try {
        const { householdId } = req.params;
        const { pricePerKg } = req.body; // Requires food price per kg in the request

        const pastLogs = await Log.aggregate([
            { $match: { householdId: new Types.ObjectId(householdId), timestamp: { $gte: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) } } },
            { $group: { _id: null, totalAmount: { $sum: "$amount" } } }
        ]);

        if (pastLogs.length === 0) {
            res.json({ estimatedCost: 0, totalAmountFed: 0 });
            return;
        }

        const totalAmountFed = pastLogs[0].totalAmount;
        const estimatedMonthlyConsumption = (totalAmountFed / 30) * 30;
        const estimatedCost = (estimatedMonthlyConsumption / 1000) * pricePerKg;

        res.json({ estimatedCost, totalAmountFed });
    } catch (error: unknown) {
        console.error("Error:", error);
        res.status(500).json({ message: "Error predicting feeding costs" });
    }
};

// Detect anomalies in feeding behavior
export const detectAnomalies = async (req: Request, res: Response): Promise<void> => {
    try {
        const { householdId } = req.params;
        const logs = await Log.find({ householdId: new Types.ObjectId(householdId) });

        if (logs.length < 5) {
            res.json({ message: "Not enough data to detect anomalies" });
            return;
        }

        const amounts = logs.map(log => log.amount);
        const avg = amounts.reduce((a, b) => a + b, 0) / amounts.length;
        const stdDev = Math.sqrt(amounts.map(x => Math.pow(x - avg, 2)).reduce((a, b) => a + b, 0) / amounts.length);

        const anomalies = logs.filter(log => Math.abs(log.amount - avg) > 2 * stdDev);

        res.json({ avgAmount: avg, stdDev, anomalies });
    } catch (error: unknown) {
        console.error("Error:", error);
        res.status(500).json({ message: "Error detecting anomalies" });
    }
};
