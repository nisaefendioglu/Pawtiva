package com.nisaefendioglu.pawtiva.core.model

/** Mascot species the user can pick. Changing it never touches progress data. */
enum class PetType { CAT, DOG }

/** Visual mood of the mascot. Derived from today's activity only, never punitive. */
enum class PetMood { HAPPY, EXCITED, NORMAL, SLEEPY, CELEBRATING }

/** Theme preference stored by the user. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }
