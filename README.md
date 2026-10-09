# Password Audit Tool (Java)

A command-line Java program that reads a file of passwords (one per line), scores each one from 0 to 100 against basic strength rules, and prints a masked report.

Built as a hands-on project while studying for CompTIA Security+.

## Scoring rules

- Length: up to 40 points
- Character variety (lowercase, uppercase, digits, symbols): 15 points each
- Penalties: password on the common-password list, a character repeated 3+ times in a row, digits only
- Ratings: STRONG (80+), MODERATE (55-79), WEAK (below 55)

Passwords are masked in the output (first and last character only).

## Run it
```
javac PasswordAudit.java
java PasswordAudit passwords.txt
```

## Sample output

See `output.txt`. The included `passwords.txt` contains synthetic test passwords only. Never run this tool on real credentials.

## Known limitations

- The common-password list has only 11 entries; a real tool would load a large list from a file.
- Scoring favors character variety over length, so a long passphrase like "correcthorsebatterystaple" rates only MODERATE even though length is a major strength factor.
- Does not check dictionary words, keyboard patterns, or leaked-password databases.

## Possible next steps

- Load a larger common-password list from a file
- Reward long passphrases more heavily
- Detect keyboard patterns and sequences (abc, 123)
- Add JUnit tests

