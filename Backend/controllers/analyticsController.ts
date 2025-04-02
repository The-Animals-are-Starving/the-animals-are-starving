import { Request, Response } from "express";
import Log, { ILog } from "../models/Log";
import Pet, { IPet } from "../models/Pet";
import { Types } from "mongoose";
import Household from "../models/Household";

export const getFeedingAnomalies = async (req: Request, res: Response): Promise<void> => {
  try {
    const { householdId } = req.params;

    let household = null;
      if (householdId) {
          household = await Household.findById(householdId);
          if (!household) {
              console.log("Household not found")
              res.status(404).json({ message: "Household not found" });
              return;
          }
      }
    

    // Get pets in the household
    const pets: IPet[] = await Pet.find({ householdId: new Types.ObjectId(householdId) });
    if (!pets.length) {
      res.json({ anomalies: [] });
      return;
    }

    const oneWeekAgo = new Date();
    oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);

    // Retrieve all logs for this household from the past week
    const logs: ILog[] = await Log.find({
      householdId: new Types.ObjectId(householdId),
      timestamp: { $gte: oneWeekAgo },
    });

    const anomalies: Array<{
      pet: string;
      largeDeviation: boolean;
      significantlyLate: boolean;
      averageAmount: number;
      feedingCount: number;
    }> = [];

    for (const pet of pets) {
      const petName = pet.name;

      const petLogs = logs.filter(log => log.petName.toString() === petName);
      let largeDeviation = false;
      let significantlyLate = false;
      let totalAmount = 0;

      if (petLogs.length > 0) {
        for (const log of petLogs) {
          totalAmount += log.amount;
        }
        const avgAmount = totalAmount / petLogs.length;
        // deviation threshold of 30% of the average
        const deviationThreshold = avgAmount * 0.3;

        for (const log of petLogs) {
          if (Math.abs(log.amount - avgAmount) > deviationThreshold) {
            largeDeviation = true;
          }

          const logDateInVan = new Date(
            log.timestamp.toLocaleString("en-US", { timeZone: "America/Vancouver" })
          );
          const [scheduledHour, scheduledMinute] = pet.feedingTime.split(":").map(Number);
          const scheduledDateInVan = new Date(logDateInVan);
          scheduledDateInVan.setHours(scheduledHour, scheduledMinute, 0, 0);
          const lateThreshold = 30 * 60 * 1000; // 30 minutes in milliseconds

          if (logDateInVan.getTime() - scheduledDateInVan.getTime() > lateThreshold) {
            significantlyLate = true;
          }
        }

        anomalies.push({
          pet: petName,
          largeDeviation,
          significantlyLate,
          averageAmount: avgAmount,
          feedingCount: petLogs.length,
        });
      } else {
        // If no logs exist for the pet in the past week, assume no anomalies
        anomalies.push({
          pet: petName,
          largeDeviation: false,
          significantlyLate: false,
          averageAmount: 0,
          feedingCount: 0,
        });
      }
    }

    res.json({ anomalies });
  } catch (error) {
    console.error("Error computing feeding anomalies:", error);
    res.status(500).json({ message: "Internal server error" });
  }
};

export const getUserRankings = async (req: Request, res: Response): Promise<void> => {
  try {
    const { householdId } = req.params;

    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfNextMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1);

    const logs: ILog[] = await Log.find({ 
      householdId: new Types.ObjectId(householdId),
      timestamp: { $gte: startOfMonth, $lt: startOfNextMonth }
    });
    
    if (!logs.length) {
      res.json({ rankings: [] });
      return;
    }

    const userTotals: { [userName: string]: number } = {};
    let overallTotal = 0;

    logs.forEach(log => {
      overallTotal += log.amount;
      const user = log.userName.toString();
      if (!userTotals[user]) {
        userTotals[user] = 0;
      }
      userTotals[user] += log.amount;
    });

    if (overallTotal === 0) {
      res.json({ rankings: [] });
      return;
    }

    // Create an array of ranking objects sorted by contribution percentage descending
    const rankings = Object.entries(userTotals)
      .map(([user, total]) => ({
        user,
        contribution: (total / overallTotal) * 100,
      }))
      .sort((a, b) => b.contribution - a.contribution);

    res.json({ rankings });
  } catch (error) {
    console.error("Error computing user rankings:", error);
    res.status(500).json({ message: "Internal server error" });
  }
};
