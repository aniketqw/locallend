# 🚀 Quick Start: Deploy to Hugging Face

Follow these steps to deploy LocalLend to Hugging Face Spaces in 10 minutes!

## ✅ Checklist

### 1. Set Up MongoDB Atlas (5 minutes)
- [ ] Go to https://www.mongodb.com/cloud/atlas
- [ ] Create free account + free cluster
- [ ] Click "Connect" → "Connect your application"
- [ ] Copy connection string: `mongodb+srv://user:password@cluster.mongodb.net/locallend`
- [ ] Save this for step 3

### 2. Get Hugging Face Token (2 minutes)
- [ ] Go to https://huggingface.co/settings/tokens
- [ ] Click "New token"
- [ ] Name: "LocalLend Deployment"
- [ ] Role: **Write** (important!)
- [ ] Copy the token (starts with `hf_`)
- [ ] Save this for step 3

### 3. Add GitHub Secrets (3 minutes)

Go to: **Your Repo → Settings → Secrets and variables → Actions → New repository secret**

Add these 6 secrets:

| Secret Name | Where to Get It | Required? |
|------------|-----------------|-----------|
| `HF_TOKEN` | Step 2 above | ✅ Yes |
| `MONGODB_URI` | Step 1 above | ✅ Yes |
| `JWT_SECRET` | Run: `openssl rand -base64 32` | ✅ Yes |
| `CLOUDINARY_CLOUD_NAME` | https://cloudinary.com/console | ✅ Yes |
| `CLOUDINARY_API_KEY` | https://cloudinary.com/console | ✅ Yes |
| `CLOUDINARY_API_SECRET` | https://cloudinary.com/console | ✅ Yes |

### 4. Deploy! (1 minute)

```bash
# Commit and push the workflow files
git add .
git commit -m "Add Hugging Face deployment"
git push origin aniket_dev
```

### 5. Watch the Magic ✨

1. Go to your GitHub repo → **Actions** tab
2. Click the running workflow
3. Watch the deployment progress (takes 5-10 minutes)
4. When done, visit: `https://huggingface.co/spaces/LocalLend/locallend`

## 🎯 That's It!

Your app will be live at: `https://locallend-locallend.hf.space`

**Test it with:**
- Email: `fasd@gmail.com`
- Password: `12345678`

## 📚 Need More Details?

See [HUGGINGFACE_DEPLOYMENT.md](HUGGINGFACE_DEPLOYMENT.md) for:
- Detailed explanations
- Troubleshooting guide
- How to make updates
- Security best practices

---

**Questions?** Check the troubleshooting section in HUGGINGFACE_DEPLOYMENT.md
