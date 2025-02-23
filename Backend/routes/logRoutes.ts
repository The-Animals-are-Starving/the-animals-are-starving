import express from "express";
import {
    logFeeding,
    getPetFeedingHistory,
    getHouseholdFeedingHistory,
    getUserFeedingHistory
} from "../controllers/logController";

const router = express.Router();

router.post("/", logFeeding);
router.get("/pet/:petId", getPetFeedingHistory);
router.get("/household/:householdId", getHouseholdFeedingHistory);
router.get("/user/:userEmail", getUserFeedingHistory);

export default router;