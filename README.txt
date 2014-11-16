HackRPI project for Nov. 2014 by Avi Weinstock and Chris Brenon.

-----
Experimental 3d game in Rust & Java
-----

The original goal was to make a simple networked 3d game (e.g. an FPS) using Rust for the server and JOGL for the client. There were some complications (compilation time for LLVM+Clang, needed to generate bindings for a physics library; and quirks of the Java Swing framework). Thus, the end product is a java program that allows the user to navigate around a 3d rainbow (randomly-colored) sphere, and also emit motion events to the rust server, which narrates in the manner of a text-based adventure ("Player #3 moves forward -5 units") to standard output.
