# Migrating SonarQube from H2 to PostgreSQL

**Date**: 2025-10-28
**SonarQube Version**: 10.7.0.96327
**Current Database**: H2 (embedded, 2.3 GB)
**Target Database**: PostgreSQL 14.19

---

## ⚠️ Important: What This Migration Does

**This is a FRESH START, not a data migration:**
- ❌ You will **lose all historical data** from H2 (scans, issues, trends)
- ✅ You will get a **clean PostgreSQL database** (production-ready)
- ✅ First scan will re-detect all issues (hundreds of thousands)
- ✅ Admin user resets to default (`admin/admin`)

**Why this is OK:**
- H2 historical data includes broken incremental analysis results
- Fresh baseline with fixed plugin is more accurate
- PostgreSQL is production-ready for ongoing use

---

## 📋 Prerequisites

Before starting:

1. ✅ **Verify current scan completes successfully** with fixed plugin
2. ✅ **Verify issue count is correct** (hundreds of thousands, not 182)
3. ✅ **Verify duplication detection works** (~20%, not 1-5%)
4. ✅ **PostgreSQL is running** (`pg_isready` shows accepting connections)
5. ✅ **You have PostgreSQL superuser access** (e.g., `postgres` user)
6. ✅ **Backup H2 data** (optional, for reference)

---

## Step 1: Backup H2 Database (Optional)

Keep your H2 data as a reference:

```bash
# Stop SonarQube first
cd /Users/bceverly/dev/sonarqube
./bin/macosx-universal-64/sonar.sh stop

# Backup H2 data directory
cp -r data data.h2.backup.2025-10-28

# Backup configuration
cp conf/sonar.properties conf/sonar.properties.h2.backup
```

---

## Step 2: Create PostgreSQL Database

**Run these SQL commands as PostgreSQL superuser:**

```bash
# Connect to PostgreSQL as superuser
psql -U postgres
```

**Then execute:**

```sql
-- 1. Create the SonarQube user with a secure password
CREATE USER sonarqube WITH PASSWORD 'ChangeThisToASecurePassword123!';

-- 2. Create the SonarQube database
CREATE DATABASE sonarqube WITH
    OWNER sonarqube
    ENCODING 'UTF8'
    LC_COLLATE 'en_US.UTF-8'
    LC_CTYPE 'en_US.UTF-8'
    TEMPLATE template0;

-- 3. Grant all privileges on the database to the user
GRANT ALL PRIVILEGES ON DATABASE sonarqube TO sonarqube;

-- 4. Connect to the sonarqube database
\c sonarqube

-- 5. Grant schema permissions
GRANT ALL ON SCHEMA public TO sonarqube;

-- 6. Verify the setup
\l sonarqube
\du sonarqube

-- 7. Exit psql
\q
```

**Verify database exists:**

```bash
psql -U sonarqube -d sonarqube -c "SELECT current_database(), current_user;"
# Should prompt for password and show: sonarqube | sonarqube
```

---

## Step 3: Configure PostgreSQL (if needed)

**Check PostgreSQL configuration:**

```bash
# Find postgresql.conf location
psql -U postgres -c "SHOW config_file;"
```

**Edit `postgresql.conf` and verify these settings:**

```conf
# Minimum requirements for SonarQube:
max_connections = 60          # SonarQube needs 60 connections
shared_buffers = 256MB        # Minimum recommended

# Optional but recommended:
work_mem = 16MB
maintenance_work_mem = 64MB
```

**If you changed settings, restart PostgreSQL:**

```bash
# Homebrew PostgreSQL
brew services restart postgresql@14

# Or if using pg_ctl
pg_ctl restart -D /opt/homebrew/var/postgresql@14
```

---

## Step 4: Update SonarQube Configuration

**Edit `/Users/bceverly/dev/sonarqube/conf/sonar.properties`:**

```bash
# Make a backup first
cp conf/sonar.properties conf/sonar.properties.backup

# Edit the file
nano conf/sonar.properties
```

**Find and uncomment/modify these lines:**

```properties
#----- PostgreSQL 9.6 or greater
sonar.jdbc.username=sonarqube
sonar.jdbc.password=ChangeThisToASecurePassword123!
sonar.jdbc.url=jdbc:postgresql://localhost/sonarqube
```

**Important:**
- Use the SAME password you set in Step 2
- Uncomment these lines (remove the `#` at the start)
- Comment out or remove any other `sonar.jdbc.*` settings

**Save and exit** (Ctrl+O, Enter, Ctrl+X in nano)

---

## Step 5: Remove H2 Database File

**Force SonarQube to use PostgreSQL by removing H2 database:**

```bash
cd /Users/bceverly/dev/sonarqube

# Rename H2 database file (don't delete, in case you need to rollback)
mv data/sonar.mv.db data/sonar.mv.db.old

# Optionally clear Elasticsearch data for fresh start
rm -rf data/es8/*
```

---

## Step 6: Start SonarQube with PostgreSQL

```bash
cd /Users/bceverly/dev/sonarqube

# Start SonarQube
./bin/macosx-universal-64/sonar.sh start

# Watch the logs
tail -f logs/sonar.log
```

**Look for these log messages:**

```
✅ "Database is PostgreSQL"
✅ "SonarQube is operational"
❌ "H2 database should be used for evaluation purpose only" (should NOT appear)
```

**Check web server logs:**

```bash
tail -f logs/web.log
```

**Wait for:**
```
INFO  web[][o.s.s.p.Platform] SonarQube is operational
```

---

## Step 7: Verify SonarQube Startup

**Check if SonarQube is running:**

```bash
# Should show "SonarQube is up" after ~30-60 seconds
./bin/macosx-universal-64/sonar.sh status

# Or check the web interface
open http://localhost:9000
```

---

## Step 8: First Login and Setup

**Login to SonarQube:**

1. Go to: http://localhost:9000
2. Login with default credentials:
   - Username: `admin`
   - Password: `admin`
3. **SonarQube will force you to change the password immediately**
4. Set a secure password

---

## Step 9: Generate New Scanner Token

**Create a new authentication token:**

1. Click on **Administrator avatar** (top right)
2. Go to **My Account** → **Security**
3. Generate a new token:
   - Name: `scanner-token` (or any name you prefer)
   - Type: **User Token**
   - Expiration: **No expiration** (or set as needed)
4. **Copy the token** - you'll need it for scanning

---

## Step 10: Deploy Fixed Mathematica Plugin

**Copy the updated plugin JAR:**

```bash
cd /Users/bceverly/dev/sonar-mathematica-plugin

# Copy fixed plugin to SonarQube
cp build/libs/sonar-mathematica-plugin-1.0.jar \
   /Users/bceverly/dev/sonarqube/extensions/plugins/

# Restart SonarQube to load the plugin
cd /Users/bceverly/dev/sonarqube
./bin/macosx-universal-64/sonar.sh restart

# Wait for restart (30-60 seconds)
tail -f logs/sonar.log
```

**Verify plugin loaded:**

1. Go to: http://localhost:9000/admin/marketplace?filter=installed
2. Look for **Mathematica** plugin
3. Should show version and status: **Installed**

---

## Step 11: Run First Scan

**Scan your Mathematica project:**

```bash
cd /Users/bceverly/dev/SLL

# Run scan with new token
sonar-scanner \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<paste-your-token-here>
```

**Expected results:**
- ✅ Scan time: ~35-60 seconds (fast, thanks to optimizations!)
- ✅ Issues found: Hundreds of thousands (correct!)
- ✅ Duplication: ~20% (correct!)
- ✅ Complexity metrics: All calculated
- ✅ No "status=SAME" file skipping

---

## Step 12: Verify Migration Success

**Check the SonarQube dashboard:**

1. Go to: http://localhost:9000/dashboard?id=sll-mathematica
2. **Verify metrics:**
   - Issues count: Hundreds of thousands ✅
   - Duplication: ~20% ✅
   - Complexity metrics visible ✅
   - Code smells, bugs, vulnerabilities all detected ✅

**Check PostgreSQL database:**

```bash
# Connect to verify data
psql -U sonarqube -d sonarqube -c "SELECT COUNT(*) FROM projects;"
psql -U sonarqube -d sonarqube -c "SELECT COUNT(*) FROM issues;"
```

Should show project and issue counts.

---

## 🎉 Migration Complete!

Your SonarQube is now running on PostgreSQL with:

- ✅ Production-ready database (PostgreSQL)
- ✅ Fixed Mathematica plugin (AST enhancements, performance optimizations)
- ✅ Accurate issue detection (no more skipped files)
- ✅ Correct duplication detection (~20%)
- ✅ Fresh, clean baseline data

---

## 🔄 Rollback Instructions (If Something Goes Wrong)

**If you need to go back to H2:**

```bash
# 1. Stop SonarQube
cd /Users/bceverly/dev/sonarqube
./bin/macosx-universal-64/sonar.sh stop

# 2. Restore H2 configuration
cp conf/sonar.properties.h2.backup conf/sonar.properties

# 3. Restore H2 database
mv data/sonar.mv.db.old data/sonar.mv.db

# 4. Start SonarQube
./bin/macosx-universal-64/sonar.sh start

# 5. Check logs
tail -f logs/sonar.log
```

---

## 📊 Database Comparison

| Feature | H2 (Old) | PostgreSQL (New) |
|---------|----------|------------------|
| **Type** | Embedded, file-based | Client-server |
| **Performance** | Good for small datasets | Better at scale |
| **Concurrent users** | Single user | Multi-user |
| **Backup** | Requires shutdown | Online backups |
| **Production ready** | ❌ Not recommended | ✅ Recommended |
| **SonarQube warning** | ⚠️ "Evaluation only" | ✅ None |
| **Your data size** | 2.3 GB | Will grow similarly |

---

## 🛠️ Troubleshooting

### Issue: "Cannot connect to PostgreSQL"

**Check:**
```bash
# Is PostgreSQL running?
pg_isready

# Can you connect manually?
psql -U sonarqube -d sonarqube

# Check sonar.properties has correct URL
grep "sonar.jdbc.url" /Users/bceverly/dev/sonarqube/conf/sonar.properties
```

### Issue: "Authentication failed"

**Check:**
- Password in `sonar.properties` matches PostgreSQL user password
- PostgreSQL user `sonarqube` exists and has correct password

**Reset password if needed:**
```sql
psql -U postgres
ALTER USER sonarqube WITH PASSWORD 'new_password';
```

### Issue: "Schema not found"

**Grant schema permissions:**
```sql
psql -U postgres -d sonarqube
GRANT ALL ON SCHEMA public TO sonarqube;
```

### Issue: Scan still finding only 182 issues

**Check:**
- Fixed plugin deployed: `ls -lh extensions/plugins/sonar-mathematica-plugin-1.0.jar`
- SonarQube restarted after plugin update
- No errors in logs: `grep ERROR logs/sonar.log`

---

## 📝 Post-Migration Checklist

After successful migration:

- [ ] Bookmark new dashboard: http://localhost:9000/dashboard?id=sll-mathematica
- [ ] Save new scanner token securely
- [ ] Update CI/CD with new token (if applicable)
- [ ] Delete H2 backup after confirming everything works (optional)
- [ ] Schedule regular PostgreSQL backups
- [ ] Document new admin password

---

## 💡 Tips for Ongoing Use

1. **Regular backups**: Set up PostgreSQL backup schedule
   ```bash
   # Example backup command
   pg_dump -U sonarqube sonarqube > sonarqube_backup_$(date +%Y%m%d).sql
   ```

2. **Monitor database size**:
   ```sql
   SELECT pg_size_pretty(pg_database_size('sonarqube'));
   ```

3. **Keep SonarQube updated**: Watch for new versions

4. **Keep plugin updated**: Rebuild after making rule changes

---

## 📞 Support

If you encounter issues:

1. Check SonarQube logs: `tail -f logs/sonar.log`
2. Check PostgreSQL logs (location varies by installation)
3. SonarQube documentation: https://docs.sonarqube.org/latest/
4. PostgreSQL documentation: https://www.postgresql.org/docs/

---

**Good luck with the migration!** 🚀
