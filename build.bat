@echo off
set exclude=Elements testing .git


setlocal EnableDelayedExpansion
set count=0
for /F "delims=" %%f in ('dir /a:d /b') do (
    set /a count+=1
    set "output[!count!]=%%f"
)

for /L %%n in (1 1 !count!) do (
    set "continue="

    for %%b in (%exclude%) do (
        if %%b EQU !output[%%n]! (
           set continue=1
        )
    )

    if not defined continue (
        javac !output[%%n]!\*.java
    )
)

javac Peer.java
javac EncryptionHandler.java