import express, { Request, Response } from "express";
import admin from "../config/firebase";
import User from "../models/User";

const router = express.Router();


async function getUserDeviceToken(email: string): Promise<string | null> {
    try {
      const user = await User.findOne({ email });
      return user?.FCMToken || null;
    } catch (error) {
      console.error("Error fetching user token:", error);
      return null;
    }
}  


//notifies user to feed the animals
router.post("/:email", async (req: Request, res: Response): Promise<void> => {
    const { email } = req.params;

    const title = "The Animals are Starving";
    const body = "Please feed the animals before they die of hunger!";
  
    if (!email) {
      res.status(400).json({ error: "email is required" });
      return;
    }
  
    try {
      const fcmtoken = await getUserDeviceToken(email);
      if (!fcmtoken) {
        res.status(404).json({ error: "User token not found" });
        return;
      }
  
      const message = {
        notification: {
          title,
          body,
        },
        token: fcmtoken
      };
  
      const response = await admin.messaging().send(message);
      res.status(200).json({ message: "Notification sent", response });
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error);
      console.error("Error sending notification:", error);
      res.status(500).json({ error: "Failed to send notification", details: errorMessage });
    }
  });
  
export default router;
