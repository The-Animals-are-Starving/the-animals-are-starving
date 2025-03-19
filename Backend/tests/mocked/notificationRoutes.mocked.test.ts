import request from "supertest";
import express from "express";
import router from "../../routes/notificationRoutes";
import User from "../../models/User";
import admin from "../../config/firebase";

// Mock external dependencies
jest.mock("../../models/User");
jest.mock("../../config/firebase", () => ({
  messaging: jest.fn(),
}));

const app = express();
app.use(express.json());
app.use("/", router);

describe("POST /:email - Notify user to feed the animals (Mocked)", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  /**
   * Test: User token not found
   * Input: A valid email parameter with no corresponding user found (or no FCMToken present)
   * Expected: HTTP 404 with error "User token not found"
   * Mock: User.findOne returns null
   */
  it("should return 404 if user token is not found", async () => {
    (User.findOne as jest.Mock).mockResolvedValue(null);

    const res = await request(app).post("/test@example.com");
    expect(res.status).toBe(404);
    expect(res.body).toEqual({ error: "User token not found" });
  });

  /**
   * Test: Successful notification send
   * Input: A valid email parameter corresponding to a user with an FCMToken
   * Expected: HTTP 200 with a JSON body containing { message: "Notification sent", response: <firebase response> }
   * Mock: User.findOne returns a user object with a valid FCMToken; admin.messaging().send returns a mock response.
   */
  it("should send notification successfully if user token exists", async () => {
    const mockFCMToken = "valid-token";
    (User.findOne as jest.Mock).mockResolvedValue({
      email: "test@example.com",
      FCMToken: mockFCMToken,
    });

    const sendMock = jest.fn().mockResolvedValue("mockResponse");
    (admin.messaging as jest.Mock).mockReturnValue({ send: sendMock });

    const res = await request(app).post("/test@example.com");

    // Verify that the notification message is constructed correctly and sent
    expect(sendMock).toHaveBeenCalledWith({
      notification: {
        title: "The Animals are Starving",
        body: "Please feed the animals before they die of hunger!",
      },
      token: mockFCMToken,
    });
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ message: "Notification sent", response: "mockResponse" });
  });

  /**
   * Test: Firebase send failure
   * Input: A valid email parameter corresponding to a user with an FCMToken, but the messaging service fails.
   * Expected: HTTP 500 with error "Failed to send notification" and error details from Firebase.
   * Mock: User.findOne returns a valid user; admin.messaging().send rejects with an error.
   */
  it("should return 500 if admin.messaging().send throws an error", async () => {
    const mockFCMToken = "valid-token";
    (User.findOne as jest.Mock).mockResolvedValue({
      email: "test@example.com",
      FCMToken: mockFCMToken,
    });

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

  /**
   * Additional Test: Simulate an error thrown by User.findOne
   * Expected: HTTP 404 with error "Failed to send notification" and error details from User.findOne failure.
   */
  it("should return 404 if User.findOne throws an error", async () => {
    jest.spyOn(User, "findOne").mockRejectedValueOnce(new Error("findOne error"));
    const res = await request(app).post("/test@example.com");
    expect(res.status).toBe(404);
    expect(res.body).toEqual({
      error: "User token not found"
    });
  });

  /**
   * Test: Firebase send failure with non-Error rejection
   * Expected: HTTP 500 with error "Failed to send notification" and details as the string of the rejection.
   */
  it("should return 500 and convert a non-Error rejection to a string (messaging send)", async () => {
    const mockFCMToken = "valid-token";
    // Return a valid user with an FCMToken so that the controller proceeds past the 404 check.
    (User.findOne as jest.Mock).mockResolvedValue({
      email: "test@example.com",
      FCMToken: mockFCMToken,
    });

    // Force admin.messaging().send to reject with a non-Error value (a string)
    const nonErrorRejection = "non error rejection";
    const sendMock = jest.fn().mockRejectedValue(nonErrorRejection);
    (admin.messaging as jest.Mock).mockReturnValue({ send: sendMock });

    const res = await request(app).post("/test@example.com");
    expect(res.status).toBe(500);
    expect(res.body).toEqual({
      error: "Failed to send notification",
      details: "non error rejection",
    });
  });
});
