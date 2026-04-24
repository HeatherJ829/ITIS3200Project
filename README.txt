Heather Joyce and Kourtney Gray-Ford Voting Systems


---HOW TO USE SECURE MENU
        Once main is open you are shown 4 options with one secret option.

        --To regsiter as a voter-

        SELECT 1 and enter username then password when prompted. The system will hash the password with SHA-256 and generate a HMAC
        over the voter record


        ---To login as a voter-

        SELECT 2 and enter established username and password.
        SELECT 1-3 matching the candidate you want to vote for. The system will generate an HMAC over your vote and store it with your
        vote in votes.txt


        ---To login as an admin-

        SELECT 3 and enter established username and password.
        You will be brought to a new menu where you can select to see the results or to go back to main menu.

        SELECT 1 and you will see the election results. At the bottom you will see if the votes passed or 
        failed the integrity check and you will be notified how many failed if any. The system re-verifys the HMAC of every vote.

        SELECT 2 and you will be brought back to main menu.


        ----To register as an admin SECRET-

        DISCLAIMER-  This function serves to create an admin account since manually putting the information in would be a pain as it goes through the 
        same hashing process as the voters. This function would not be included in a true voting system, it is just for testing.

        SELECT 3200 and enter a username and password when prompted the system will save your password and create an HMAC in the exact same way as
        voter registration.

FILES:
    voters.txt -- Stores registered voter information and HMAC
    votes.txt -- Stores the vote casted and HMAC
    admin.txt -- Stores admin login information
    votersBROKEN.txt -- Stores registered voter informatin no HMAC
    votesBROKEN.txt -- Stores the vote casted no HMAC\
    adminBROKEN.txt -- Stores admin login information

TEST CASES
    - Open Secure.java
    - Register as a voter
    - Login and vote for Alice
    - Try to login again ~Attempt Failed
    - Open voter.txt and change the true variable on the voters information to false
    - Try to login again ~Attempt Failed with message alerting the tampered account
    - Register as a different voter
    - Login and vote for Alice
    - Open votes.txt and change Alice to Evil Mallory
    - Login as admin and check the results
    - Alert of tampered vote, vote not counted ~Attempt Failed
    - Open votes.txt and change Evil Mallory back to Alice
    - Copy that vote entry and duplicate it to simulate duplicate entries
    - Login as admin and check results
    - Duplicate votes detected and not counted ~ Attempt Failed

Mechanism

    generateHMAC()
        This is a core function used throughout secure.java. It takes a string of data and the secret key and produces a hex string
        using the HMACSHA256 algorithm in javax.crytpo. The goal of this function is to create a tamper seal so we know if any data
        has be chnaged since the first HMAC was created. Normally the secret key would be kept in a seperate secure area, but for this 
        we have it in the file, without this key, the attacker can not forge a valid HMAC.

    sha256()
        This is also a core function used throughout secure.java. None of the passwords are saved in plaintext. This helper function
        hashes the input using SHA256 from Javas MessageDigest class. During login, the password entered is hashed and compared to the
        stored hash function.
    
    registerVoter()
        When a voter registers, their password is hashed with SHA256 before being saved. Additionally, an HMAC is computed over their entire
        record (username,password,hasVoted) and stored as a seperate field in voters.txt. This protects all variables in the file from being
        tampered with.

    findVoter()
        Before returning any voter records, this method recomputes the HMAC and compares it to the stored value. If someone opens voters.txt
        and changes true back to false to vote a second time, the HMAC will no longer match, blocking the login.

    castVote()
        When a voter votes for someone, the system creates a string of "username|candidate" and computes an HMAC over it. Both the vote and the
        HMAC are saved into votes.txt. If any of the information is changed, the HMAC will no longer match and breaks the signature.
    
    markVoterAsVoted()
        After a voter casts a vote, the hasVoted flag in the voters data is updated from false to true. Since the voter record is protected with HMAC,
        a new HMAC is recomputed over the updated record and saved. This keeps the HMAC in sync with the altered data.

    viewResults()
        When an admin views results, every vote in votes.txt is re-verified before being counted. The system recomputes the expected HMAC for each entry,
        and compares it to the stored value. Votes that pass the check are counted and votes that are flagged as tampered are excluded and counted in its 
        own variable. This function also uses a HashSet tracks which voter IDs have already been counted. If a voter ID appears more than once in votes.txt, 
        the duplicate entry is flagged and excluded even if its HMAC is valid.

Broken system
    In Broken.java there are no protections in place besides the SHA256 hashing on passwords. Since there is no integrity protections from HMAC, there 
    are many things at risk to attackers. An attacker can access votes.txt and duplicate votes or change who a person votes for. They can also access
    voters.txt and altar the hasVoted flag attached to the voter data and vote as many times as they want from a single login.