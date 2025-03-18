import request from "supertest";
import express from "express";
import mongoose from "mongoose";
import { MongoMemoryServer } from "mongodb-memory-server";
import router from "../../routes/notificationRoutes";
import User from "../../models/User";
import admin from "../../config/firebase";

//mocking firebase since we don't want to send actual notifications
jest.mock("../../config/firebase", () => ({
  messaging: jest.fn(),
}));

const app = express();
app.use(express.json());
app.use("/", router);

let mongoServer: MongoMemoryServer;

beforeAll(async () => {
  // Set up in-memory MongoDB
  mongoServer = await MongoMemoryServer.create();
  const uri = mongoServer.getUri();
  await mongoose.connect(uri);
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

afterEach(async () => {
  // Clear the users collection and mocks between tests
  await User.deleteMany({});
  jest.clearAllMocks();
});

describe("POST /:email - Notify user to feed the animals (without mocks)", () => {
  /**
   * Test: When no user with the given email (or FCMToken) exists.
   * Input: Email parameter that doesn't match any user.
   * Expected Output: HTTP 404 with JSON { error: "User token not found" }.
   */
  it("should return 404 if user token is not found", async () => {
    const res = await request(app).post("/nonexistent@example.com");
    expect(res.status).toBe(404);
    expect(res.body).toEqual({ error: "User token not found" });
  });

  /**
   * Test: Successfully sending a notification.
   * Input: A valid email corresponding to a user with a dummy FCMToken.
   * Expected Output: HTTP 200 with JSON { message: "Notification sent", response: <firebase response> }.
   * Mock Behavior: Firebase Admin messaging's send method returns a mock response.
   */
  it("should send notification successfully if user token exists", async () => {
    const dummyToken = "dummy_fcm_token";
    await new User({ name: "Alice", email: "test@example.com", FCMToken: dummyToken }).save();

    // Set up Firebase Admin messaging mock to simulate a successful send
    const sendMock = jest.fn().mockResolvedValue("mockResponse");
    (admin.messaging as jest.Mock).mockReturnValue({ send: sendMock });

    const res = await request(app).post("/test@example.com");

    // Verify the notification message structure
    expect(sendMock).toHaveBeenCalledWith({
      notification: {
        title: "The Animals are Starving",
        body: "Please feed the animals before they die of hunger!",
      },
      token: dummyToken,
    });
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ message: "Notification sent", response: "mockResponse" });
  });

  /**
   * Test: Firebase messaging failure.
   * Input: A valid email with a dummy FCMToken, but messaging.send throws an error.
   * Expected Output: HTTP 500 with JSON { error: "Failed to send notification", details: <error message> }.
   * Mock Behavior: Firebase Admin messaging's send method rejects with an error.
   */
  it("should return 500 if firebase messaging send fails", async () => {
    const dummyToken = "dummy_fcm_token";
    await new User({ name: "Alice", email: "test@example.com", FCMToken: dummyToken }).save();

    const errorMessage = "Firebase error";
    const sendMock = jest.fn().mockRejectedValue(new Error(errorMessage));
    (admin.messaging as jest.Mock).mockReturnValue({ send: sendMock });

    const res = await request(app).post("/test@example.com");
    expect(res.status).toBe(500);
    expect(res.body).toEqual({
      error: "Failed to send notification",
      details: errorMessage,
    });
  });
});