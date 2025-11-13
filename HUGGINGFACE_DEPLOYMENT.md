# LocalLend - Hugging Face Deployment Guide

This guide will help you deploy the LocalLend application to Hugging Face Spaces with automatic updates when you push to your GitHub repository.

## 📋 Prerequisites

Before you begin, make sure you have:

1. **GitHub Repository** with your LocalLend code
2. **Hugging Face Account** - Sign up at https://huggingface.co
3. **MongoDB Atlas Account** - Free tier available at https://www.mongodb.com/cloud/atlas
4. **Cloudinary Account** - Free tier available at https://cloudinary.com

## 🔧 Step-by-Step Setup

### Step 1: Set Up MongoDB Atlas (Free Cloud MongoDB)

Since Hugging Face Spaces runs in a container without persistent storage, we need to use MongoDB Atlas:

1. Go to https://www.mongodb.com/cloud/atlas/register
2. Create a free account and cluster
3. Click "Connect" → "Connect your application"
4. Copy the connection string (looks like: `mongodb+srv://username:password@cluster.mongodb.net/locallend`)
5. Replace `<password>` with your actual password
6. Add `/locallend` at the end to specify the database name

**Example:** `mongodb+srv://myuser:mypassword@cluster0.abc123.mongodb.net/locallend?retryWrites=true&w=majority`

### Step 2: Get Hugging Face Token

1. Go to https://huggingface.co/settings/tokens
2. Click "New token"
3. Give it a name like "LocalLend Deployment"
4. Select role: **Write** (required for creating and updating spaces)
5. Click "Generate token"
6. **Copy the token** (you won't see it again!)

### Step 3: Configure GitHub Secrets

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add the following secrets one by one:

| Secret Name | Description | Example |
|------------|-------------|---------|
| `HF_TOKEN` | Your Hugging Face token | `hf_xxxxxxxxxxxxx` |
| `MONGODB_URI` | MongoDB Atlas connection string | `mongodb+srv://user:pass@cluster.mongodb.net/locallend` |
| `JWT_SECRET` | Random string for JWT (min 32 chars) | `dbgkasubfdlsidbfviwaebpug89w75y947y59723824y` |
| `CLOUDINARY_CLOUD_NAME` | From Cloudinary dashboard | `dt2puub22` |
| `CLOUDINARY_API_KEY` | From Cloudinary dashboard | `446449233874948` |
| `CLOUDINARY_API_SECRET` | From Cloudinary dashboard | `Y7g295QCnTJx0OnGx_jJP1i3vqM` |

**To generate a secure JWT_SECRET:**
```bash
# On macOS/Linux
openssl rand -base64 32

# Or use any random string generator (minimum 32 characters)
```

### Step 4: Update Hugging Face Space Name (Optional)

If you want to change the space name from "LocalLend/locallend":

1. Open `.github/workflows/deploy-huggingface.yml`
2. Find line 12: `HF_REPO_ID: LocalLend/locallend`
3. Change to your preferred name: `YourUsername/your-space-name`

**Note:** The format is `username/space-name` where username is your Hugging Face username.

### Step 5: Deploy!

Now you're ready to deploy. You have two options:

#### Option A: Push to trigger automatic deployment

```bash
# Make sure you're on the aniket_dev branch (or main)
git checkout aniket_dev

# Commit your changes
git add .
git commit -m "Add Hugging Face deployment"

# Push to GitHub (this will trigger the workflow)
git push origin aniket_dev
```

#### Option B: Manual trigger from GitHub

1. Go to your GitHub repository
2. Click **Actions** tab
3. Select **Deploy LocalLend to Hugging Face Space** workflow
4. Click **Run workflow** → Select branch → **Run workflow**

## 🎯 Monitoring Deployment

### GitHub Actions

1. Go to your GitHub repository → **Actions** tab
2. Click on the latest workflow run
3. Watch the deployment progress in real-time
4. Each step will show ✅ when completed

### Hugging Face Space

1. Once deployment starts, go to: `https://huggingface.co/spaces/LocalLend/locallend`
   (or your custom space name)
2. You'll see the build logs
3. First build takes 5-10 minutes (subsequent builds are faster)
4. Once complete, your app will be available at: `https://locallend-locallend.hf.space`

## 🔍 Troubleshooting

### Deployment Failed: "HF_TOKEN secret is not set"
- Make sure you added `HF_TOKEN` to GitHub Secrets (Settings → Secrets and variables → Actions)
- Token must have **Write** permissions

### Deployment Failed: "Could not create space"
- The space name might already exist
- Try changing `HF_REPO_ID` in the workflow file to a unique name

### App deployed but can't connect to database
- Check MongoDB Atlas connection string in GitHub Secrets
- Make sure you:
  - Replaced `<password>` with actual password
  - Added your IP to MongoDB Atlas Network Access (or allow 0.0.0.0/0 for testing)
  - Added `/locallend` at the end of connection string

### Images not uploading
- Verify Cloudinary credentials in GitHub Secrets
- Check all three values: `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`

### Frontend shows but API returns errors
- Wait 2-3 minutes after deployment completes (backend may still be starting)
- Check Hugging Face Space logs for backend errors
- Verify all environment variables are set in Hugging Face Space settings

## 📝 Making Updates

After initial deployment, any push to `aniket_dev` or `main` branch will automatically:
1. Build the application
2. Upload to Hugging Face
3. Restart the space with new code

**Workflow:**
```bash
# Make your code changes
git add .
git commit -m "Your update message"
git push origin aniket_dev

# Wait 5-10 minutes for deployment to complete
# Visit your space URL to see changes
```

## 🌐 Accessing Your Deployed App

Once deployed, your app will be available at:
- **Space URL**: `https://huggingface.co/spaces/LocalLend/locallend`
- **App URL**: `https://locallend-locallend.hf.space`

### Test Credentials
- **Email**: `fasd@gmail.com`
- **Password**: `12345678`

## 🔐 Security Notes

1. **Never commit secrets** to your repository
2. All sensitive data should be in GitHub Secrets
3. MongoDB Atlas free tier has IP restrictions - configure Network Access
4. Consider making your Hugging Face Space private if it contains sensitive data

## 📊 Space Settings

To configure your Hugging Face Space after deployment:

1. Go to your space: `https://huggingface.co/spaces/LocalLend/locallend`
2. Click **Settings**
3. You can:
   - View/Edit environment variables
   - Change space visibility (Public/Private)
   - Configure hardware (upgrade for better performance)
   - View build logs
   - Restart the space

## 🎓 Additional Resources

- [Hugging Face Spaces Documentation](https://huggingface.co/docs/hub/spaces)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [MongoDB Atlas Documentation](https://www.mongodb.com/docs/atlas/)
- [Cloudinary Documentation](https://cloudinary.com/documentation)

## ❓ Common Questions

**Q: How much does Hugging Face Spaces cost?**
A: The free tier is sufficient for small projects. You get persistent space and community hardware.

**Q: Can I use my own domain?**
A: Yes, Hugging Face Spaces supports custom domains in paid tiers.

**Q: How do I update environment variables after deployment?**
A: Update GitHub Secrets, then push any change to trigger redeployment. Or update directly in Hugging Face Space settings.

**Q: My space is sleeping/inactive**
A: Free tier spaces sleep after inactivity. They wake up when someone visits. Consider upgrading for 24/7 availability.

---

**Need Help?**
- Check GitHub Actions logs for deployment issues
- Check Hugging Face Space logs for runtime issues
- Ensure all GitHub Secrets are correctly set
