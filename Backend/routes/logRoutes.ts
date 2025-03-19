import express from "express";
import {
    logFeeding,
    getPetFeedingHistory,
    getHouseholdFeedingHistory,
    getUserFeedingHistory
} from "../controllers/logController";

const router = express.Router();

router.post("/:petName", logFeeding);
router.get("/pet/:petName", getPetFeedingHistory);
router.get("/household/:householdId", getHouseholdFeedingHistory);
router.get("/user/:userEmail", getUserFeedingHistory);

export default router;