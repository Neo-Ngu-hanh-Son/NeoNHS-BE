# Bug Fix: Workshop Tag/Image Cascade & OrphanRemoval Errors

## Error 1: ObjectDeletedException

**Error Type:** `org.hibernate.ObjectDeletedException`

**Full Message:** 
```
deleted object would be re-saved by cascade (remove deleted object from associations) 
for entity [fpt.project.NeoNHS.entity.WorkshopTag]
```

**When it occurs:** When updating a WorkshopTemplate with new tags or images

### Root Cause (First Issue)

The error occurred due to improper handling of entity collections with cascade operations - explicitly deleting entities that are still in Hibernate's persistence context, then trying to save the parent entity with cascade.

---

## Error 2: OrphanRemoval Collection Replacement

**Error Type:** `org.hibernate.HibernateException`

**Full Message:**
```
A collection with orphan deletion was no longer referenced by the owning entity instance: 
fpt.project.NeoNHS.entity.WorkshopTemplate.workshopImages
```

**When it occurs:** When updating images/tags after adding `orphanRemoval = true`

### Root Cause (Second Issue)

After adding `orphanRemoval = true`, you **cannot replace a collection** with `setXxx(newList)`. Hibernate tracks the collection reference and loses it when you replace the entire collection, causing it to fail handling orphan removal.

---

## Solution

### The Complete Fix

**CORRECT Implementation:**
```java
// Clear existing images (orphanRemoval will delete them)
if (template.getWorkshopImages() != null) {
    template.getWorkshopImages().clear();
} else {
    template.setWorkshopImages(new ArrayList<>());
}

// Add new images to the EXISTING collection - ✅ Use add(), not setXxx()
for (int i = 0; i < request.getImageUrls().size(); i++) {
    WorkshopImage image = WorkshopImage.builder()
            .imageUrl(request.getImageUrls().get(i))
            .isThumbnail(i == thumbnailIndex)
            .workshopTemplate(template)
            .build();
    template.getWorkshopImages().add(image);  // ✅ Add to existing collection
}
```

**Entity Configuration:**
```java
@OneToMany(mappedBy = "workshopTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
private List<WorkshopImage> workshopImages;
```

---

## Files Modified

### 1. WorkshopTemplateServiceImpl.java
- Line ~281-305: Image update - use `clear()` + `add()`
- Line ~307-332: Tag update - use `clear()` + `add()`

### 2. WorkshopTemplate.java
- Added `orphanRemoval = true` to all `@OneToMany` relationships

---

## Best Practices for orphanRemoval

1. ✅ **DO:** Use `collection.clear()` to remove items
2. ✅ **DO:** Use `collection.add(item)` to add to existing collection
3. ❌ **DON'T:** Replace collection with `setCollection(newList)`
4. ❌ **DON'T:** Explicitly delete entities managed by cascade

**Key Rule:** When `orphanRemoval = true`, modify the existing collection, never replace it.

---

**Date Fixed:** February 21, 2026  
**Status:** ✅ RESOLVED
