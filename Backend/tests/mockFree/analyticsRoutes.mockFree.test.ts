import request from "supertest";
import express from "express";
import mongoose from "mongoose";
import { MongoMemoryServer } from "mongodb-memory-server";
import { Types } from "mongoose";
import Log from "../../models/Log";
import Pet from "../../models/Pet";
import Household from "../../models/Household";
import router from "../../routes/analyticsRoutes";

const app = express();
app.use(express.json());
app.use(router);

let mongoServer: MongoMemoryServer;

beforeAll(async () => {
  mongoServer = await MongoMemoryServer.create();
  const uri = mongoServer.getUri();
  await mongoose.connect(uri);
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

afterEach(async () => {
  await Log.deleteMany({});
  await Pet.deleteMany({});
  await Household.deleteMany({});
});

describe("POST /anomalies/:householdId (Unmocked)", () => {
  it("should return 404 if household not found", async () => {
    const res = await request(app).post("/anomalies/507f1f77bcf86cd799439012");
    expect(res.status).toBe(404);
    expect(res.body.message).toBe("Household not found");
  });

  it("should return anomalies with empty array if no pets found", async () => {
    const household = new Household({ 
      _id: "507f1f77bcf86cd799439011", 
      name: "TestHouse",
      managerId: new mongoose.Types.ObjectId()
    });
    await household.save();

    const res = await request(app).post(`/anomalies/${household._id}`);
    expect(res.status).toBe(200);
    expect(res.body.anomalies).toEqual([]);
  });

  it("should return anomalies for pets with no logs", async () => {
    const household = new Household({ 
      _id: "507f1f77bcf86cd799439011", 
      name: "TestHouse",
      managerId: new mongoose.Types.ObjectId()
    });
    await household.save();
    const pet = new Pet({ name: "Fluffy", feedingTime: "10:00", householdId: household._id });
    await pet.save();

    const res = await request(app).post(`/anomalies/${household._id}`);
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
    const household = new Household({ 
      _id: "507f1f77bcf86cd799439011", 
      name: "TestHouse",
      managerId: new mongoose.Types.ObjectId()
    });
    await household.save();
    const pet = new Pet({ name: "Fluffy", feedingTime: "10:00", householdId: household._id });
    await pet.save();
  
    // Assume Vancouver is UTC-7; for a feeding scheduled at 10:00 Vancouver time,
    // use a UTC timestamp of 17:00:00Z. Similarly, a log at 10:45 Vancouver is 17:45:00Z.
    const log1 = new Log({
      petName: "Fluffy",
      userName: "Jim",
      amount: 5,
      householdId: household._id,
      timestamp: new Date("2025-04-01T17:00:00Z"),
    });
    const log2 = new Log({
      petName: "Fluffy",
      userName: "Jim",
      amount: 10,
      householdId: household._id,
      timestamp: new Date("2025-04-01T17:45:00Z"),
    });
    await log1.save();
    await log2.save();
  
    const res = await request(app).post(`/anomalies/${household._id}`);
    // Calculations:
    // - Average amount: (5 + 10) / 2 = 7.5
    // - Both logs deviate by 2.5 from the average, exceeding the 30% threshold,
    //   so largeDeviation = true.
    // - Log2 is 45 minutes late (> 30 minutes threshold), so significantlyLate = true.
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
    const res = await request(app).post("/anomalies/invalidHouseholdId");
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Internal server error");
  });
});

describe("GET /rankings/:householdId (Unmocked)", () => {
  it("should return empty rankings if no logs found", async () => {
    const res = await request(app).get("/rankings/507f1f77bcf86cd799439011");
    expect(res.status).toBe(200);
    expect(res.body.rankings).toEqual([]);
  });

  it("should return empty rankings if overall total is 0", async () => {
    const log1 = new Log({
      userName: "Alice",
      petName: "Fido",
      amount: 0,
      householdId: "507f1f77bcf86cd799439011",
      timestamp: new Date(),
    });
    const log2 = new Log({
      userName: "Bob",
      petName: "Fido", 
      amount: 0,
      householdId: "507f1f77bcf86cd799439011",
      timestamp: new Date(),
    });
    await log1.save();
    await log2.save();

    const res = await request(app).get("/rankings/507f1f77bcf86cd799439011");
    expect(res.status).toBe(200);
    expect(res.body.rankings).toEqual([]);
  });

  it("should compute user rankings from logs", async () => {
    const log1 = new Log({
      userName: "Alice",
      petName: "Fido",
      amount: 10,
      householdId: "507f1f77bcf86cd799439011",
      timestamp: new Date(),
    });
    const log2 = new Log({
      userName: "Bob",
      petName: "Fido",
      amount: 30,
      householdId: "507f1f77bcf86cd799439011",
      timestamp: new Date(),
    });
    await log1.save();
    await log2.save();

    const res = await request(app).get("/rankings/507f1f77bcf86cd799439011");
    expect(res.status).toBe(200);
    expect(res.body.rankings).toEqual([
      { user: "Bob", contribution: 75 },
      { user: "Alice", contribution: 25 },
    ]);
  });

  it("should return 500 if an error occurs in rankings", async () => {
    const res = await request(app).get("/rankings/invalidHouseholdId");
    expect(res.status).toBe(500);
    expect(res.body.message).toBe("Internal server error");
  });
});
