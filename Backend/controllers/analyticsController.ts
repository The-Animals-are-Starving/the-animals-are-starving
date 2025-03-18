import { Request, Response } from "express";
import Log, { ILog } from "../models/Log";
import { Types } from "mongoose";

// Helper function to get the ISO week number of a given date
function getWeekNumber(date: Date): number {
  const d = new Date(date.getTime());
  d.setHours(0, 0, 0, 0);
  // Adjust to nearest Thursday (ISO week rule)
  d.setDate(d.getDate() + 4 - (d.getDay() || 7));
  const yearStart = new Date(d.getFullYear(), 0, 1);
  const weekNo = Math.ceil((((d.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
  return weekNo;
}

// Compute user rankings based on feeding contributions using three nested loops
export const getUserRankings = async (req: Request, res: Response): Promise<void> => {
  try {
    const { householdId } = req.params;
    if (!householdId) {
        res.status(400).json({ message: "householdId is required" });
        return;
    }

    // Retrieve all logs for the given household
    const logs: ILog[] = await Log.find({ householdId: new Types.ObjectId(householdId) });
    if (!logs.length) {
        res.json({ userContributions: {} });
        return;
    }

    // Group logs by pet and then by week
    const petWeeklyLogs: { [petName: string]: { [week: number]: ILog[] } } = {};
    logs.forEach(log => {
      const pet = log.petName.toString();
      const week = getWeekNumber(log.timestamp);
      if (!petWeeklyLogs[pet]) {
        petWeeklyLogs[pet] = {};
      }
      if (!petWeeklyLogs[pet][week]) {
        petWeeklyLogs[pet][week] = [];
      }
      petWeeklyLogs[pet][week].push(log);
    });

    // Compute user contributions with three nested loops:
    // Loop 1: Iterate over pets.
    // Loop 2: Iterate over weeks for each pet.
    // Loop 3: Iterate over each log for that week.
    const userContributions: { [userName: string]: { [petName: string]: Array<{ week: number; contribution: number }> } } = {};
    for (const pet in petWeeklyLogs) {
      for (const week in petWeeklyLogs[pet]) {
        const weekNumber = Number(week);
        const weekLogs = petWeeklyLogs[pet][weekNumber];
        const totalFood = weekLogs.reduce((sum, log) => sum + log.amount, 0);
        if (totalFood === 0) continue;

        weekLogs.forEach(log => {
          const contribution = (log.amount / totalFood) * 100;
          const user = log.userName.toString();
          if (!userContributions[user]) {
            userContributions[user] = {};
          }
          if (!userContributions[user][pet]) {
            userContributions[user][pet] = [];
          }
          userContributions[user][pet].push({ week: weekNumber, contribution });
        });
      }
    }

    res.json({ userContributions });
  } catch (error) {
    console.error("Error computing user rankings:", error);
    res.status(500).json({ message: "Internal server error" });
  }
};

// // Predict feeding cost for the next month
// export const predictFeedingCost = async (req: Request, res: Response): Promise<void> => {
//     try {
//         const { householdId } = req.params;
//         const { pricePerKg } = req.body; // Requires food price per kg in the request

//         const pastLogs = await Log.aggregate([
//             { $match: { householdId: new Types.ObjectId(householdId), timestamp: { $gte: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) } } },
//             { $group: { _id: null, totalAmount: { $sum: "$amount" } } }
//         ]);

//         if (pastLogs.length === 0) {
//             res.json({ estimatedCost: 0, totalAmountFed: 0 });
//             return;
//         }

//         const totalAmountFed = pastLogs[0].totalAmount;
//         const estimatedMonthlyConsumption = (totalAmountFed / 30) * 30;
//         const estimatedCost = (estimatedMonthlyConsumption / 1000) * pricePerKg;

//         res.json({ estimatedCost, totalAmountFed });
//     } catch (error: unknown) {
//         console.error("Error:", error);
//         res.status(500).json({ message: "Error predicting feeding costs" });
//     }
// };

// // Detect anomalies in feeding behavior
// export const detectAnomalies = async (req: Request, res: Response): Promise<void> => {
//     try {
//         const { householdId } = req.params;
//         const logs = await Log.find({ householdId: new Types.ObjectId(householdId) });

//         if (logs.length < 5) {
//             res.json({ message: "Not enough data to detect anomalies" });
//             return;
//         }

//         const amounts = logs.map(log => log.amount);
//         const avg = amounts.reduce((a, b) => a + b, 0) / amounts.length;
//         const stdDev = Math.sqrt(amounts.map(x => Math.pow(x - avg, 2)).reduce((a, b) => a + b, 0) / amounts.length);

//         const anomalies = logs.filter(log => Math.abs(log.amount - avg) > 2 * stdDev);

//         res.json({ avgAmount: avg, stdDev, anomalies });
//     } catch (error: unknown) {
//         console.error("Error:", error);
//         res.status(500).json({ message: "Error detecting anomalies" });
//     }
// };
