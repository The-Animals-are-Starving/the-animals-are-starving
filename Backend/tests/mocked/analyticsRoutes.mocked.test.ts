import request from "supertest";
import express from "express";
import { Types } from "mongoose";
import router from "../../routes/analyticsRoutes";
import Log from "../../models/Log";
import Pet from "../../models/Pet";
import Household from "../../models/Household";

jest.mock("../../models/Log");
jest.mock("../../models/Pet");
jest.mock("../../models/Household");

const app = express();
app.use(express.json());
// Mount the router so that all routes are available.
app.use(router);

const validHouseholdId = "507f1f77bcf86cd799439011";

describe("Analytics Routes", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("POST /anomalies/:householdId", () => {
    it("should return 404 if household not found", async () => {
      (Household.findById as jest.Mock).mockResolvedValue(null);
      const res = await request(app).post(`/anomalies/${validHouseholdId}`);
      expect(res.status).toBe(404);
      expect(res.body.message).toBe("Household not found");
    });

    it("should return anomalies with empty array if no pets found", async () => {
      (Household.findById as jest.Mock).mockResolvedValue({ _id: validHouseholdId });
      (Pet.find as jest.Mock).mockResolvedValue([]);
      const res = await request(app).post(`/anomalies/${validHouseholdId}`);
      expect(res.status).toBe(200);
      expect(res.body.anomalies).toEqual([]);
    });

    it("should return anomalies for pets with no logs", async () => {
      (Household.findById as jest.Mock).mockResolvedValue({ _id: validHouseholdId });
      const pets = [{ _id: "pet1", name: "Fluffy", feedingTime: "10:00" }];
      (Pet.find as jest.Mock).mockResolvedValue(pets);
      // No logs for this pet in the past week.
      (Log.find as jest.Mock).mockResolvedValue([]);
      const res = await request(app).post(`/anomalies/${validHouseholdId}`);
      expect(res.status).toBe(200);
      expect(res.body.anomalies).toEqual([
        {
          pet: "Fluffy",
          largeDeviation: false,
          significantlyLate: false,
          averageAmount: 0,
          feedingCount: 0,
        },
      ]);
    });

    it("should compute anomalies for pets with logs", async () => {
      (Household.findById as jest.Mock).mockResolvedValue({ _id: validHouseholdId });
    
      // Feeding time is scheduled at 10:00 AM in Vancouver time
      // That is 17:00 UTC on April 1, 2025 (Vancouver is UTC-7 during PDT)
      const pets = [
        { _id: "pet1", name: "Fluffy", feedingTime: "2025-04-01T17:00:00.000Z" }
      ];
      (Pet.find as jest.Mock).mockResolvedValue(pets);
    
      // Logs:
      // - First log: exactly on time (10:00 AM PDT → 17:00 UTC)
      // - Second log: 45 minutes later (17:45 UTC)
      const scheduledDate = new Date("2025-04-01T17:00:00.000Z");
      const lateDate = new Date("2025-04-01T17:45:00.000Z");
    
      const logs = [
        { petName: "Fluffy", amount: 5, householdId: new Types.ObjectId(validHouseholdId), timestamp: scheduledDate },
        { petName: "Fluffy", amount: 10, householdId: new Types.ObjectId(validHouseholdId), timestamp: lateDate },
      ];
      (Log.find as jest.Mock).mockResolvedValue(logs);

      const res = await request(app).post(`/anomalies/${validHouseholdId}`);
      // Expected calculations:
      // - Average amount: (5 + 10) / 2 = 7.5
      // - Deviation threshold: 7.5 * 0.3 = 2.25 (both logs deviate by 2.5, so largeDeviation = true)
      // - The 45-minute delay should result in significantlyLate = true.
      expect(res.status).toBe(200);
      expect(res.body.anomalies).toEqual([
        {
          pet: "Fluffy",
          largeDeviation: true,
          significantlyLate: true,
          averageAmount: 7.5,
          feedingCount: 2,
        },
      ]);
    });    

    it("should return 500 if an error occurs in anomalies", async () => {
      (Household.findById as jest.Mock).mockRejectedValue(new Error("DB error"));
      const res = await request(app).post(`/anomalies/${validHouseholdId}`);
      expect(res.status).toBe(500);
      expect(res.body.message).toBe("Internal server error");
    });
  });

  describe("GET /rankings/:householdId", () => {
    it("should return empty rankings if no logs found", async () => {
      (Log.find as jest.Mock).mockResolvedValue([]);
      const res = await request(app).get(`/rankings/${validHouseholdId}`);
      expect(res.status).toBe(200);
      expect(res.body.rankings).toEqual([]);
    });

    it("should return empty rankings if overall total is 0", async () => {
      const logs = [
        { userName: "Alice", amount: 0, timestamp: new Date() },
        { userName: "Bob", amount: 0, timestamp: new Date() }
      ];
      (Log.find as jest.Mock).mockResolvedValue(logs);
      const res = await request(app).get(`/rankings/${validHouseholdId}`);
      expect(res.status).toBe(200);
      expect(res.body.rankings).toEqual([]);
    });

    it("should compute user rankings from logs", async () => {
      const logs = [
        { userName: "Alice", amount: 10, timestamp: new Date() },
        { userName: "Bob", amount: 30, timestamp: new Date() }
      ];
      (Log.find as jest.Mock).mockResolvedValue(logs);
      const res = await request(app).get(`/rankings/${validHouseholdId}`);
      expect(res.status).toBe(200);
      expect(res.body.rankings).toEqual([
        { user: "Bob", contribution: 75 },
        { user: "Alice", contribution: 25 },
      ]);
    });

    it("should return 500 if an error occurs in rankings", async () => {
      (Log.find as jest.Mock).mockRejectedValue(new Error("DB error"));
      const res = await request(app).get(`/rankings/${validHouseholdId}`);
      expect(res.status).toBe(500);
      expect(res.body.message).toBe("Internal server error");
    });
  });
});