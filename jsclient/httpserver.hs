module Main where
import System.Environment
import System.IO
import qualified Data.ByteString.Lazy as L
import qualified Network.HTTP.Types as HTTP
import qualified Network.Wai as Wai
import qualified Network.Wai.Handler.Warp as Warp

fileServer :: L.ByteString -> Wai.Application
fileServer content request respond = respond $ Wai.responseLBS HTTP.status200 [] content

serve port content = Warp.run port (fileServer content)

main = do
    args <- getArgs
    case args of
        [port] -> L.hGetContents stdin >>= serve (read port)
        [port, filename] -> L.readFile filename >>= serve (read port)
        _ -> hPutStr stderr "Usage: httpserver port [file]\n"
