# Quick Start - Image Upload Feature

## 🚀 5-Minute Setup

### 1. Get Cloudinary Credentials (2 minutes)

1. Go to https://cloudinary.com/ and sign up (free)
2. Copy these from your dashboard:
   - Cloud Name
   - API Key
   - API Secret

### 2. Configure Backend (1 minute)

**Step 1: Create `.env` file:**

```bash
cp .env.example .env
```

**Step 2: Edit `.env` with your Cloudinary credentials:**

Open `.env` in a text editor and update:

```bash
CLOUDINARY_CLOUD_NAME=your_actual_cloud_name
CLOUDINARY_API_KEY=your_actual_api_key
CLOUDINARY_API_SECRET=your_actual_api_secret
```

💡 **Tip**: Your `.env` file is automatically ignored by Git, so your secrets are safe!

**Alternative - Environment Variables (for production):**

```powershell
# Windows PowerShell
$env:CLOUDINARY_CLOUD_NAME="your_cloud_name"
$env:CLOUDINARY_API_KEY="your_api_key"
$env:CLOUDINARY_API_SECRET="your_api_secret"
```

```bash
# Linux/Mac
export CLOUDINARY_CLOUD_NAME="your_cloud_name"
export CLOUDINARY_API_KEY="your_api_key"
export CLOUDINARY_API_SECRET="your_api_secret"
```

### 3. Start the Application (2 minutes)

```bash
# Start backend
mvn spring-boot:run

# Start frontend (in another terminal)
cd locallend-frontend
npm run dev
```

### 4. Verify Setup

Check if Cloudinary is configured:
```bash
curl http://localhost:8080/api/images/health
```

Should return:
```json
{"configured": true, "message": "Cloudinary is properly configured"}
```

## ✅ You're Done!

Now you can:
1. Go to "Add Item" page
2. Upload images (up to 5 per item)
3. See images displayed on item cards

## 📝 What Was Implemented

### Backend
- `CloudinaryConfig.java` - Cloudinary configuration
- `ImageService.java` - Image upload/delete logic
- `ImageController.java` - REST API endpoints

### Frontend
- `AddItemPage.tsx` - Image upload UI with preview

### Endpoints
- `POST /api/images/upload` - Upload single image
- `POST /api/images/upload-multiple` - Upload multiple images
- `DELETE /api/images?url={url}` - Delete image
- `GET /api/images/health` - Check configuration

## 🔥 Features

✅ Multiple image upload (max 5)
✅ Image preview before upload
✅ Drag and drop support
✅ File validation (type, size)
✅ Progress indicators
✅ Remove images before submission
✅ Display in all item views
✅ Fallback placeholder
✅ Cloud storage (no local files)
✅ CDN delivery (fast loading)
✅ Automatic optimization

## 🐛 Troubleshooting

**Problem**: "Cloudinary not configured" error
**Solution**: Check credentials and restart backend

**Problem**: Images not uploading
**Solution**: Ensure JWT token is valid and user is logged in

**Problem**: Images not displaying
**Solution**: Check browser console, verify image URLs are valid

## 📚 Full Documentation

See `IMAGE_UPLOAD_SETUP.md` for complete details.

## 🎉 Success!

Your LocalLend application now supports image uploads!

Users can add photos to items and see them displayed throughout the app.
