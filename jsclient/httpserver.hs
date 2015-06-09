module Main where
import System.Environment
import System.IO
import qualified Data.ByteString.Lazy as L
import qualified Network.HTTP.Types as HTTP
import qualified Network.Wai as Wai
import qualified Network.Wai.Handler.Warp as Warp

fileServer :: L.ByteString -> Wai.Application
fileServer content request respond = respond $ Wai.responseLBS HTTP.status200 [] content

main = do
    args <- getArgs
    case args of
        [port, filename] -> do
            file <- L.readFile filename
            Warp.run (read port) (fileServer file)
        _ -> hPutStr stderr "Usage: httpserver port file\n"
