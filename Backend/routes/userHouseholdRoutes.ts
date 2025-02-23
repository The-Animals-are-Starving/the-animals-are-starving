import express from "express";
import { createHousehold, addUserToHousehold, removeUserFromHousehold } from "../controllers/userHouseholdController";

const router = express.Router();

router.post("/create", createHousehold);
router.post("/add-user", addUserToHousehold);
router.post("/remove-user", removeUserFromHousehold);

export default router;