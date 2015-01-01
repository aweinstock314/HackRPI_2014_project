#!/usr/bin/env runghc
-- Simple script that connects to the server and runs in circles.
import Network
import GHC.IO.Handle
import Control.Monad
import Control.Concurrent (threadDelay)

delayMs = threadDelay . (*) 1000

moveForward :: Num a => Show a => a -> String
moveForward n = "{\"variant\":\"MoveForward\",\"fields\":[" ++ show n ++ "]}\n"

rotateCamera :: Num a => Show a => a -> a -> String
rotateCamera theta phi = "{\"variant\":\"RotateCamera\",\"fields\":[{\"_field0\":" ++ show theta ++ ",\"_field1\":" ++ show phi ++ "}]}\n"

turnAndMove :: Handle -> IO ()
turnAndMove h = mapM_ (hPutStr h) [moveForward 0.1, rotateCamera (-0.005) 0]

main = do
    h <- connectTo "localhost" (PortNumber 51701)
    forever $ do
        turnAndMove h
        delayMs 10
