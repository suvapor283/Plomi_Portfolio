# Project Database Schema

This document outlines the main database schema for the 'Plomi' project and explains the design intent behind each
table.

---

## 1. Users Table (User Information)

#### Description: This table stores the basic information of users who have signed up for the service. It is a core table for user authentication and identification.

> **Fields**:
> * `id`: Unique ID to distinguish each user (**PK**, **Auto-increment**)
> * `email`: Login ID / Email address (**Unique**, **Required**)
> * `password`: Encrypted password (**Required**)
> * `nickname`: User nickname (**Unique**, **Required**, max **20** chars)
> * `profile_image_url`: URL for the user's profile picture (Optional)
> * `status_message`: Status message (Optional, max **100** chars)
> * `role`: User authority (Either `USER` or `ADMIN`, defaults to `USER` automatically)
> * `created_at`: Timestamp for user account creation (Automatically recorded)
> * `updated_at`: Timestamp for last information update (Automatically updated)

* *`email` and `nickname` have `UNIQUE` constraints to prevent duplicate registrations with identical information.*
* *Designed to centrally manage core membership information and ensure accurate user identification with essential
  data.*

---

## 2. Diaries Table (Diary Entry Information)

#### Description: This table stores information about diary entries written by members.

> **Fields**:
> * `id`: Unique ID distinguishing each diary entry (**PK**, **Auto-increment**)
> * `title`: Title of the diary entry (**Required**)
> * `content`: Body of the diary entry (`TEXT` type for long content, **Required**)
> * `date`: Date the diary entry was written (**Required**)
> * `author_id`: ID of the diary's author (References `Users` table, can be `NULL` if author deactivates)
> * `is_private`: Public/private status of the diary (Defaults to `FALSE` for public)
> * `diary_likes_count`: Number of likes for this diary entry (Cached field, defaults to `0`)
> * `view_count`: Number of views for this diary entry (Defaults to `0`)
> * `created_at`: Timestamp for initial diary entry creation
> * `updated_at`: Timestamp for last diary entry update

* *`CONSTRAINT unique_author_date UNIQUE (author_id, date)`: Restricts a user to
  writing **only one diary entry per day**.*
* *`CONSTRAINT fk_diary_author FOREIGN KEY (author_id) REFERENCES Users(id) ON DELETE SET NULL`: If a member
  deactivates,
  the `author_id` for their diary entries will be set to `NULL`. This keeps the entries from being deleted, but they
  would typically be handled as "This post does not exist." or similar.*
* *`diary_likes_count` is synchronized by the application logic whenever changes occur in the `DiaryLikes` table.*
* *`view_count` is incremented by application logic each time a diary entry is viewed.*

---

## 3. Schedules Table (Personal Schedule Information)

#### Description: This table stores personal schedule information for users.

> * **Fields**:
> * `id`: Unique ID distinguishing each schedule (**PK**, **Auto-increment**)
> * `title`: Schedule title (**Required**)
> * `description`: Detailed description for the schedule (`TEXT` type for long content, Optional)
> * `start_time`: Schedule start time (**Required**)
> * `end_time`: Schedule end time (**Required**)
> * `owner_id`: User ID representing the owner of the schedule (References `Users` table, schedules are deleted if owner
    deactivates)
> * `created_at`: Timestamp for schedule creation
> * `updated_at`: Timestamp for last schedule update

* *`CONSTRAINT fk_schedule_owner FOREIGN KEY (owner_id) REFERENCES Users(id) ON DELETE CASCADE`: If the owner of a
  schedule deactivates their account, all schedule information belonging to that owner will be deleted from the
  database.*
* *Given that schedule data is personal, it was decided that there's no need for the data to persist without an owner,
  thus ensuring data cleanup.*
* *By using the naming convention `owner_id`, the intent to clearly indicate that this `user_id` is the primary owner of
  the schedule was made explicit.*

---

## 4. Comments Table (Comment and Reply Information)

#### Description: This table stores comment and reply information associated with diary entries. It is designed to facilitate user interaction and maintain a natural conversation flow.

> * **Fields**:
> * `id`: Unique ID distinguishing each comment (**PK**, **Auto-increment**)
> * `content`: Comment content (**Required**)
> * `author_id`: User ID of the comment author (References `Users` table, can be `NULL` if author deactivates)
> * `diary_id`: ID of the diary entry the comment belongs to (References `Diaries` table, comments are deleted if diary
    is deleted)
> * `parent_comment_id`: ID of the parent comment for replies (Self-referencing `Comments` table, can be `NULL` if
    parent comment is deleted)
> * `is_deleted`: **Flag for logical (soft) deletion of the comment** (Defaults to `FALSE` for not deleted)
> * `comment_likes_count`: Number of likes for this comment (Cached field, defaults to `0`)
> * `created_at`: Timestamp for initial comment creation
> * `updated_at`: Timestamp for last comment update

* *`CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES Users(id) ON DELETE SET NULL`: To preserve the
  context of existing comments while protecting personal information, if a comment author deactivates, the comment
  itself is retained and only the `author_id` is changed to `NULL`.*
* *`CONSTRAINT fk_comment_diary FOREIGN KEY (diary_id) REFERENCES Diaries(id) ON DELETE CASCADE`: If a diary entry is
  deleted, all comments and replies associated with that entry are physically deleted from the database.*
* *`CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES Comments(id) ON DELETE SET NULL`: Even if a
  parent comment is **soft-deleted (`is_deleted=TRUE`)**, replies maintain their relationship with the parent. This
  allows replies to still be displayed even if the parent comment is marked as "This comment has been deleted," and
  `ON DELETE SET NULL` acts as a safeguard
  against **exceptional cases where the parent comment is physically deleted**.*
* *The soft-delete flag (`is_deleted`) is used to preserve original content and maintain reply connections, allowing
  flexible display of deleted comments.*
* *When a user 'deletes' a comment, the `is_deleted` flag is set to `TRUE` and the `content` is **updated by the
  application** to 'This comment has been deleted.' or similar.*
* *`comment_likes_count` is synchronized by the application logic whenever changes occur in the `CommentLikes` table.*

---

## 5. DiaryLikes Table (Diary Like Records)

#### Description: This table stores records of users liking diary entries.

> * **Fields**:
> * `diary_id`: ID of the diary entry that received the 'like' (References `Diaries` table, like record is deleted if
    diary is deleted)
> * `user_id`: ID of the user who pressed 'like' (References `Users` table, like record is deleted if user deactivates)
> * `created_at`: Timestamp when the 'like' was pressed

* *`PRIMARY KEY (diary_id, user_id)`: `diary_id` and `user_id` are designated as a **Composite Primary Key** to restrict
  a single user from liking the same diary entry multiple times. No separate unique ID is needed.*
* *`CONSTRAINT fk_like_diary FOREIGN KEY (diary_id) REFERENCES Diaries(id) ON DELETE CASCADE`: If a diary entry is
  deleted, all 'like' records for that diary entry are also deleted.*
* *`CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE`: If a user deactivates, all '
  like' records made by that user are also deleted.*
* *'Liking' is a record of a specific user action. Therefore, it was decided that the record should disappear if the
  subject (user) or object (diary) of the action ceases to exist, which is consistent with data integrity.*
* *The `diary_likes_count` field in the `Diaries` table is synchronized according to changes in this `DiaryLikes`
  table.*

---

## 6. CommentLikes Table (Comment Like Records)

#### Description: This table manages records of users liking comments.

> * **Fields**:
> * `comment_id`: ID of the comment that received the 'like' (References `Comments` table, like record is deleted if
    comment is deleted)
> * `user_id`: ID of the user who pressed 'like' (References `Users` table, like record is deleted if user deactivates)
> * `created_at`: Timestamp when the 'like' was pressed

* *`PRIMARY KEY (comment_id, user_id)`: `comment_id` and `user_id` are designated as a **Composite Primary Key** to
  restrict a single user from liking the same comment multiple times. No separate unique ID is needed.*
* *`CONSTRAINT fk_cl_comment FOREIGN KEY (comment_id) REFERENCES Comments(id) ON DELETE CASCADE`: If a comment is *
  *physically deleted**, all 'like' records for that comment are also deleted.*
* *`CONSTRAINT fk_cl_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE`: If a user deactivates, all '
  like' records made by that user are also deleted.*
* *'Comment liking' is also a record of a specific user action; thus, it was decided that the record should disappear if
  the subject or object of the action ceases to exist, ensuring data consistency.*
* *If a comment is **soft-deleted (`is_deleted=TRUE`)**, its like records remain physically. They are only removed upon
  physical deletion of the comment.*
* *The `comment_likes_count` field in the `Comments` table is synchronized according to changes in this `CommentLikes`
  table.*
