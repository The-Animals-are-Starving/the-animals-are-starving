import request from "supertest";
import express from "express";
import mongoose from "mongoose";
import { MongoMemoryServer } from "mongodb-memory-server";
import router from "../../routes/analyticsRoutes";
import Log from "../../models/Log";
import { Types } from "mongoose";

const app = express();
app.use(express.json());
app.use("/", router);

let mongoServer: MongoMemoryServer;

beforeAll(async () => {
  // Start an in-memory MongoDB instance
  mongoServer = await MongoMemoryServer.create();
  const uri = mongoServer.getUri();
  await mongoose.connect(uri);
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

afterEach(async () => {
  // Clear the Log collection after each test.
  await Log.deleteMany({});
});

describe("GET /rankings/:householdId - Analytics Route (without mocks)", () => {
  /**
   * Test: When no logs exist for a given household,
   * Expected: HTTP 200 and an empty userContributions object.
   */
  it("should return empty contributions if no logs exist", async () => {
    const householdId = new Types.ObjectId().toHexString();
    const res = await request(app).get(`/rankings/${householdId}`);
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ userContributions: {} });
  });

  /**
   * Test: When logs exist for a household,
   * Insert two logs for the same pet on the same date for different users.
   * Expected: Computed contributions with each user contributing 50% for pet "Fluffy".
   */
  it("should return computed user contributions for existing logs", async () => {
    const householdIdObj = new Types.ObjectId();
    const householdId = householdIdObj.toHexString();
    const logDate = new Date("2023-01-10");
    await Log.create([
      {
        petName: "Fluffy",
        timestamp: logDate,
        amount: 50,
        userName: "Alice",
        householdId: householdIdObj,
      },
      {
        petName: "Fluffy",
        timestamp: logDate,
        amount: 50,
        userName: "Bob",
        householdId: householdIdObj,
      },
    ]);
    const res = await request(app).get(`/rankings/${householdId}`);
    expect(res.status).toBe(200);
    expect(res.body).toEqual({
      userContributions: {
        "Alice": {
          "Fluffy": [{ week: 2, contribution: 50 }],
        },
        "Bob": {
          "Fluffy": [{ week: 2, contribution: 50 }],
        },
      },
    });
  });

  /**
   * Verify that logs with a timestamp on a Sunday correctly compute the ISO week number.
   * For example, January 1, 2023 is a Sunday and should compute to week 52.
   * Expected: The computed week for the log is 52 with 100% contribution.
   */
  it("should compute correct week number (52) for logs with timestamp on a Sunday", async () => {
    const householdIdObj = new Types.ObjectId();
    const householdId = householdIdObj.toHexString();
    await Log.create([
      {
        petName: "Fluffy",
        timestamp: new Date("2023-01-01T12:00:00Z"), // Sunday: should adjust to week 52
        amount: 100,
        userName: "Alice",
        householdId: householdIdObj,
      }
    ]);
    const res = await request(app).get(`/rankings/${householdId}`);
    expect(res.status).toBe(200);
    expect(res.body).toEqual({
      userContributions: {
        "Alice": {
          "Fluffy": [{ week: 52, contribution: 100 }],
        },
      },
    });
  });

  /**
   * Test the branch that skips contribution calculation when totalFood is 0.
   * Insert two logs with amount 0 for the same pet.
   * Expected: Since totalFood is 0, no contributions are added, and an empty contributions object is returned.
   */
  it("should skip contribution calculation when totalFood is 0", async () => {
    const householdIdObj = new Types.ObjectId();
    const householdId = householdIdObj.toHexString();
    await Log.create([
      {
        petName: "Fluffy",
        timestamp: new Date("2023-01-10T12:00:00Z"),
        amount: 0,
        userName: "Alice",
        householdId: householdIdObj,
      },
      {
        petName: "Fluffy",
        timestamp: new Date("2023-01-10T13:00:00Z"),
        amount: 0,
        userName: "Bob",
        householdId: householdIdObj,
      }
    ]);
    const res = await request(app).get(`/rankings/${householdId}`);
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ userContributions: {} });
  });
});
