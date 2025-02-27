import express from "express";
import { createUser, getUser, updateUser, deleteUser } from "../controllers/userController";

const router = express.Router();

router.post("/", createUser);
router.get("/:email", getUser);
router.patch("/:email", updateUser);
router.delete("/:email", deleteUser);

export default router;
