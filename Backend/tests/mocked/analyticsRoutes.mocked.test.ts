import request from "supertest";
import express from "express";
import router from "../../routes/analyticsRoutes";
import Log from "../../models/Log";
import { Types } from "mongoose";
import { getUserRankings } from "../../controllers/analyticsController";
import { Request, Response } from "express";

jest.mock("../../models/Log");

const app = express();
app.use(express.json());
app.use("/", router);

describe("GET /rankings/:householdId - Analytics Route (mocked)", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  /**
   * Test: When no logs are found for a valid householdId,
   * Expected: HTTP 200 with JSON { userContributions: {} }.
   */
  it("should return empty contributions if no logs are found", async () => {
    const householdId = new Types.ObjectId().toHexString();
    // Mock Log.find to return an empty array.
    (Log.find as jest.Mock).mockResolvedValue([]);

    const res = await request(app).get(`/rankings/${householdId}`);
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ userContributions: {} });
    // Verify the query was called with a new ObjectId based on the provided string.
    expect(Log.find).toHaveBeenCalledWith({ householdId: new Types.ObjectId(householdId) });
  });

  /**
   * Test: When logs exist for a household,
   * Create two logs for the same pet on the same date for two different users.
   * Expected: The computed contributions should be 50% for both users.
   */
  it("should return computed user contributions", async () => {
    const householdId = new Types.ObjectId().toHexString();
    const logDate = new Date("2023-01-10"); // This date computes to week 2 based on our helper
    const logs = [
      {
        petName: "Fluffy",
        timestamp: logDate,
        amount: 50,
        userName: "Alice",
        householdId: new Types.ObjectId(householdId),
      },
      {
        petName: "Fluffy",
        timestamp: logDate,
        amount: 50,
        userName: "Bob",
        householdId: new Types.ObjectId(householdId),
      },
    ];
    (Log.find as jest.Mock).mockResolvedValue(logs);

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
   * Test: When an error occurs in the database call,
   * Expected: HTTP 500 with a JSON error message.
   */
  it("should return 500 if an error occurs", async () => {
    const householdId = new Types.ObjectId().toHexString();
    (Log.find as jest.Mock).mockRejectedValue(new Error("DB error"));

    const res = await request(app).get(`/rankings/${householdId}`);
    expect(res.status).toBe(500);
    expect(res.body).toEqual({ message: "Internal server error" });
  });

  /**
   * Verify that logs with a timestamp on a Sunday correctly compute the ISO week number.
   * For example, January 1, 2023 is a Sunday and should compute to week 52.
   * Input: A log with timestamp "2023-01-01T12:00:00Z" and amount 100.
   * Expected: The computed week should be 52 and the contribution for the user should be 100%.
   */
  it("should compute correct week number (52) for logs with timestamp on a Sunday (2023-01-01)", async () => {
    const householdId = new Types.ObjectId().toHexString();
    const logs = [
      {
        petName: "Fluffy",
        timestamp: new Date("2023-01-01T12:00:00Z"), // Sunday
        amount: 100,
        userName: "Alice",
        householdId: new Types.ObjectId(householdId),
      }
    ];
    (Log.find as jest.Mock).mockResolvedValue(logs);

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
   * Input: Two logs with amount 0 for the same pet.
   * Expected: Since totalFood is 0, the loop should skip adding contributions, resulting in an empty contributions object.
   */
  it("should skip contribution calculation when totalFood is 0", async () => {
    const householdId = new Types.ObjectId().toHexString();
    const logs = [
      {
        petName: "Fluffy",
        timestamp: new Date("2023-01-10T12:00:00Z"),
        amount: 0,
        userName: "Alice",
        householdId: new Types.ObjectId(householdId),
      },
      {
        petName: "Fluffy",
        timestamp: new Date("2023-01-10T13:00:00Z"),
        amount: 0,
        userName: "Bob",
        householdId: new Types.ObjectId(householdId),
      }
    ];
    (Log.find as jest.Mock).mockResolvedValue(logs);

    const res = await request(app).get(`/rankings/${householdId}`);
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ userContributions: {} });
  });
});

describe("getUserRankings - Missing householdId (mocked with direct controller invocation)", () => {
  it("should return 400 if householdId is not provided", async () => {
    // Create a fake request without householdId
    const req = { params: {} } as Request;

    // Create a fake response with jest.fn() for status and json
    const res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn()
    } as unknown as Response;

    await getUserRankings(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ message: "householdId is required" });
  });
});