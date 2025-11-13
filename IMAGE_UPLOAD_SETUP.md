# Image Upload Implementation Guide - Cloudinary

This document provides setup instructions for the Cloudinary-based image upload feature in LocalLend.

## 🎯 Overview

The image upload feature allows users to upload photos when creating items. Images are stored in Cloudinary (a cloud-based image management service) and displayed across all item views.

## 📋 Features Implemented

### Backend (Spring Boot)
- ✅ Cloudinary SDK integration
- ✅ Image upload endpoint (`POST /api/images/upload`)
- ✅ Multiple image upload endpoint (`POST /api/images/upload-multiple`)
- ✅ Image deletion endpoint (`DELETE /api/images`)
- ✅ Health check endpoint (`GET /api/images/health`)
- ✅ File validation (type, size, format)
- ✅ Automatic image optimization
- ✅ Secure URL generation

### Frontend (React + TypeScript)
- ✅ File selection with preview
- ✅ Drag-and-drop support (via file input)
- ✅ Multiple image upload (max 5 per item)
- ✅ Image preview before upload
- ✅ Progress indicators
- ✅ Remove images before submission
- ✅ Display images in item cards
- ✅ Fallback placeholder for items without images

## 🚀 Setup Instructions

### Step 1: Create a Cloudinary Account

1. Go to [https://cloudinary.com/](https://cloudinary.com/)
2. Click "Sign Up for Free"
3. Complete the registration
4. Verify your email address

### Step 2: Get Your Cloudinary Credentials

1. Log in to your Cloudinary dashboard
2. Navigate to the Dashboard (https://cloudinary.com/console)
3. Find your account details:
   - **Cloud Name**: Usually displayed at the top
   - **API Key**: Found in the "Account Details" section
   - **API Secret**: Click "Reveal" next to API Secret

### Step 3: Configure Backend

#### Recommended Method: Using `.env` File

This is the best approach for development as it keeps all your secrets in one place and out of Git.

**Step 1: Create your `.env` file:**

```bash
# Copy the example file
cp .env.example .env
```

**Step 2: Edit `.env` with your actual credentials:**

Open the `.env` file and update these lines:

```bash
CLOUDINARY_CLOUD_NAME=your_actual_cloud_name
CLOUDINARY_API_KEY=your_actual_api_key
CLOUDINARY_API_SECRET=your_actual_api_secret
```

**That's it!** The `.env` file is automatically:
- ✅ Loaded by Spring Boot on startup
- ✅ Ignored by Git (already in `.gitignore`)
- ✅ Safe from accidental commits

#### Alternative Methods:

<details>
<summary><b>Option B: Environment Variables (Click to expand)</b></summary>

Set environment variables in your terminal session.

**Windows (PowerShell):**
```powershell
$env:CLOUDINARY_CLOUD_NAME="your_cloud_name"
$env:CLOUDINARY_API_KEY="your_api_key"
$env:CLOUDINARY_API_SECRET="your_api_secret"
```

**Linux/Mac:**
```bash
export CLOUDINARY_CLOUD_NAME="your_cloud_name"
export CLOUDINARY_API_KEY="your_api_key"
export CLOUDINARY_API_SECRET="your_api_secret"
```

⚠️ **Note**: These variables only last for the current terminal session.

</details>

<details>
<summary><b>Option C: System Environment Variables (Production)</b></summary>

For production deployments, set permanent system environment variables through your OS or cloud platform:

- **Windows**: System Properties → Environment Variables
- **Linux/Mac**: Add to `~/.bashrc` or `~/.zshrc`
- **Docker**: Use `--env-file` or docker-compose `env_file`
- **Cloud Platforms**: Use their environment variable dashboards (Heroku, AWS, Azure, etc.)

</details>

⚠️ **Security Warning**: Never commit real credentials to version control! Always use environment variables or `.env` files (which are gitignored).

### Step 4: Install Dependencies

The Cloudinary dependency has already been added to `pom.xml`:

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.38.0</version>
</dependency>
```

Run Maven install:
```bash
mvn clean install
```

### Step 5: Start the Backend

```bash
mvn spring-boot:run
```

Verify Cloudinary is configured:
```bash
curl http://localhost:8080/api/images/health
```

Expected response:
```json
{
  "configured": true,
  "message": "Cloudinary is properly configured"
}
```

### Step 6: Test Image Upload

You can test the image upload without the frontend:

```bash
curl -X POST http://localhost:8080/api/images/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "folder=locallend/items"
```

## 🎨 Frontend Usage

The frontend has been automatically configured. Users can now:

1. Navigate to "Add Item" page
2. Fill in item details
3. Click "Choose Files" to select images (max 5)
4. Preview selected images
5. Remove unwanted images using the × button
6. Submit the form
7. Images are automatically uploaded before item creation

## 📁 File Structure

### Backend Files Created/Modified:

```
src/main/java/com/locallend/locallend/
├── config/
│   └── CloudinaryConfig.java          # Cloudinary bean configuration
├── controller/
│   └── ImageController.java           # Image upload/delete endpoints
├── service/
│   └── ImageService.java              # Image processing logic
└── resources/
    └── application.properties         # Cloudinary credentials
```

### Frontend Files Modified:

```
locallend-frontend/src/pages/
└── AddItemPage.tsx                    # Added image upload UI
```

## 🔒 Security Considerations

1. **Never commit credentials** to version control
2. Use **environment variables** in production
3. The backend validates:
   - File type (only images)
   - File size (max 10MB)
   - Number of files (max 5 per upload)
4. All uploads require **JWT authentication**
5. Images are stored in a **secure** Cloudinary folder

## 🎯 API Endpoints

### Upload Single Image
```
POST /api/images/upload
Content-Type: multipart/form-data
Authorization: Bearer {token}

Body:
- file: image file
- folder: "locallend/items" (optional)

Response:
{
  "success": true,
  "url": "https://res.cloudinary.com/...",
  "filename": "image.jpg"
}
```

### Upload Multiple Images
```
POST /api/images/upload-multiple
Content-Type: multipart/form-data
Authorization: Bearer {token}

Body:
- files[]: array of image files
- folder: "locallend/items" (optional)

Response:
{
  "success": true,
  "urls": ["url1", "url2", ...],
  "count": 2
}
```

### Delete Image
```
DELETE /api/images?url={cloudinary_url}
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "Image deleted successfully"
}
```

### Health Check
```
GET /api/images/health

Response:
{
  "configured": true,
  "message": "Cloudinary is properly configured"
}
```

## 🐛 Troubleshooting

### "Cloudinary not configured" error

**Cause**: Credentials not set or incorrect

**Solution**:
1. Verify credentials in Cloudinary dashboard
2. Check environment variables are set
3. Restart the backend application
4. Check `/api/images/health` endpoint

### Images not displaying

**Cause**: CORS or URL issues

**Solution**:
1. Check browser console for errors
2. Verify image URLs are valid Cloudinary URLs
3. Check CORS settings in `ImageController.java`

### Upload fails with 413 error

**Cause**: File too large

**Solution**:
- Maximum file size is 10MB
- Compress images before upload
- Check `application.properties` for size limits:
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
```

### "Invalid file format" error

**Cause**: Trying to upload non-image files

**Solution**:
- Only JPG, PNG, GIF, and WebP are supported
- Ensure file has correct MIME type

## 📊 Cloudinary Dashboard

Monitor your usage:
1. Visit https://cloudinary.com/console
2. View:
   - **Storage**: How much space you're using
   - **Bandwidth**: Monthly transfer limit
   - **Transformations**: Image optimization count
   - **Media Library**: All uploaded images

## 🎁 Cloudinary Free Tier Limits

- **Storage**: 25 GB
- **Bandwidth**: 25 GB/month
- **Transformations**: 25,000/month
- **Images**: Unlimited

## 🔄 Future Enhancements

Potential improvements:
- [ ] Image cropping/editing before upload
- [ ] Drag-and-drop file upload
- [ ] Image compression on frontend
- [ ] Multiple image carousel view
- [ ] Image zoom/lightbox
- [ ] Automatic thumbnail generation
- [ ] Image moderation/filtering

## 📚 Additional Resources

- [Cloudinary Documentation](https://cloudinary.com/documentation)
- [Spring Boot File Upload Guide](https://spring.io/guides/gs/uploading-files/)
- [React File Upload Best Practices](https://react.dev/learn)

## ✅ Testing Checklist

- [ ] Cloudinary account created
- [ ] Credentials configured
- [ ] Backend starts without errors
- [ ] `/api/images/health` returns configured=true
- [ ] Can upload single image
- [ ] Can upload multiple images
- [ ] Images display in item cards
- [ ] Can remove images before upload
- [ ] File validation works (size, type)
- [ ] Authentication required for upload

## 🤝 Support

If you encounter issues:
1. Check this documentation
2. Review browser/server console logs
3. Verify Cloudinary credentials
4. Check network requests in browser DevTools
5. Contact the development team

---

**Last Updated**: November 13, 2025
**Version**: 1.0.0
