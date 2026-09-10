package com.example.common.network

import com.example.common.model.DaySchedule
import com.example.common.model.EnrolledCourse
import com.example.common.model.RoutineClassItem
import com.example.common.model.RoutineExamItem
import com.example.common.model.RoutineItemType
import com.example.common.model.SubjectItem
import com.example.common.model.UserProfile

/**
 * Central Database & Dynamic Content Generator for Shikho Syllabus & Student Accounts.
 * Ensures the entire application (Profile, Home, Routine, Subjects, and Courses)
 * is 100% database-driven according to the logged-in student's account and syllabus.
 */
object SyllabusDatabaseManager {

    /**
     * Map student class string to Shikho Class Code
     */
    fun getClassCode(studentClass: String): String {
        return when {
            studentClass.contains("এইচএসসি") || studentClass.contains("HSC") || studentClass.contains("11") || studentClass.contains("১১") -> "C11"
            studentClass.contains("১০") || studentClass.contains("10") || studentClass.contains("এসএসসি") -> "C10"
            studentClass.contains("৯") || studentClass.contains("9") -> "C09"
            studentClass.contains("৮") || studentClass.contains("8") -> "C08"
            studentClass.contains("৭") || studentClass.contains("7") -> "C07"
            studentClass.contains("৬") || studentClass.contains("6") -> "C06"
            studentClass.contains("৫") || studentClass.contains("5") -> "C05"
            studentClass.contains("এডমিশন") -> "CAD"
            else -> "C11"
        }
    }

    /**
     * Generates Enrolled Courses for the logged-in student's class
     */
    fun getEnrolledCoursesForProfile(profile: UserProfile): List<EnrolledCourse> {
        val classCode = getClassCode(profile.studentClass)
        return when (classCode) {
            "C05" -> listOf(
                EnrolledCourse(
                    id = "c05_enrolled_main",
                    title = "ক্লাস ৫ - বার্ষিক ও প্রাথমিক মেধা অন্বেষণ প্রস্তুতি",
                    badge = "ভর্তি হয়েছো",
                    instructor = "মোজাম্মেল হক",
                    totalClasses = 90,
                    completedClasses = 8,
                    colorPrimaryHex = 0xFF0284C7,
                    colorSecondaryHex = 0xFF38BDF8
                )
            )

            "C06" -> listOf(
                EnrolledCourse(
                    id = "c06_enrolled_main",
                    title = "ক্লাস ৬ - বার্ষিক পরীক্ষার পূর্ণাঙ্গ প্রস্তুতি (২০২৬)",
                    badge = "ভর্তি হয়েছো",
                    instructor = "রাকিব হাসান ও টিম",
                    totalClasses = 110,
                    completedClasses = 12,
                    colorPrimaryHex = 0xFF0284C7,
                    colorSecondaryHex = 0xFF38BDF8
                ),
                EnrolledCourse(
                    id = "c06_enrolled_math",
                    title = "ক্লাস ৬ গণিত ও বিজ্ঞান মাস্টারক্লাস",
                    badge = "৩ দিন ট্রায়াল",
                    instructor = "মোজাম্মেল হক",
                    totalClasses = 45,
                    completedClasses = 4,
                    colorPrimaryHex = 0xFF10B981,
                    colorSecondaryHex = 0xFF34D399
                )
            )

            "C07" -> listOf(
                EnrolledCourse(
                    id = "c07_enrolled_main",
                    title = "ক্লাস ৭ - বার্ষিক পরীক্ষার পূর্ণাঙ্গ প্রস্তুতি (২০২৬)",
                    badge = "ভর্তি হয়েছো",
                    instructor = "মেহেদী হাসান",
                    totalClasses = 115,
                    completedClasses = 8,
                    colorPrimaryHex = 0xFF059669,
                    colorSecondaryHex = 0xFF34D399
                )
            )

            "C08" -> listOf(
                EnrolledCourse(
                    id = "c08_enrolled_main",
                    title = "ক্লাস ৮ - বার্ষিক পরীক্ষা ও জেএসসি ফাউন্ডেশন প্রস্তুতি",
                    badge = "ভর্তি হয়েছো",
                    instructor = "হাসান জামিল",
                    totalClasses = 120,
                    completedClasses = 15,
                    colorPrimaryHex = 0xFF4F46E5,
                    colorSecondaryHex = 0xFF818CF8
                )
            )

            "C09" -> {
                if (profile.group.contains("বিজ্ঞান") || profile.group.contains("Science")) {
                    listOf(
                        EnrolledCourse(
                            id = "c09_sci_enrolled_main",
                            title = "ক্লাস ৯ বিজ্ঞান - গণিত ও পদার্থবিজ্ঞান ফাউন্ডেশন",
                            badge = "ভর্তি হয়েছো",
                            instructor = "অপূর্ব অপু ও টিম",
                            totalClasses = 135,
                            completedClasses = 18,
                            colorPrimaryHex = 0xFF2563EB,
                            colorSecondaryHex = 0xFF60A5FA
                        )
                    )
                } else if (profile.group.contains("ব্যবসায়") || profile.group.contains("Business")) {
                    listOf(
                        EnrolledCourse(
                            id = "c09_bus_enrolled_main",
                            title = "ক্লাস ৯ ব্যবসায় শিক্ষা - হিসাববিজ্ঞান বেসিক",
                            badge = "ভর্তি হয়েছো",
                            instructor = "জাভেদ করিম",
                            totalClasses = 110,
                            completedClasses = 10,
                            colorPrimaryHex = 0xFFD97706,
                            colorSecondaryHex = 0xFFFBBF24
                        )
                    )
                } else {
                    listOf(
                        EnrolledCourse(
                            id = "c09_hum_enrolled_main",
                            title = "ক্লাস ৯ মানবিক - ইতিহাস ও ভূগোল ফাউন্ডেশন",
                            badge = "ভর্তি হয়েছো",
                            instructor = "মোশতাক আহমেদ ও টিম",
                            totalClasses = 105,
                            completedClasses = 12,
                            colorPrimaryHex = 0xFF7C3AED,
                            colorSecondaryHex = 0xFFA78BFA
                        )
                    )
                }
            }

            "C10" -> {
                if (profile.group.contains("বিজ্ঞান") || profile.group.contains("Science")) {
                    listOf(
                        EnrolledCourse(
                            id = "c10_sci_enrolled_main",
                            title = "এসএসসি বিজ্ঞান - পূর্ণাঙ্গ রিভিশন ও টেস্ট পেপার সলভ",
                            badge = "ভর্তি হয়েছো",
                            instructor = "রাহাত স্যার ও টিম",
                            totalClasses = 150,
                            completedClasses = 32,
                            colorPrimaryHex = 0xFF7C3AED,
                            colorSecondaryHex = 0xFFA78BFA
                        )
                    )
                } else if (profile.group.contains("ব্যবসায়") || profile.group.contains("Business")) {
                    listOf(
                        EnrolledCourse(
                            id = "c10_bus_enrolled_main",
                            title = "এসএসসি ব্যবসায় শিক্ষা - হিসাববিজ্ঞান ও ফিন্যান্স",
                            badge = "ভর্তি হয়েছো",
                            instructor = "কবির হোসেন",
                            totalClasses = 125,
                            completedClasses = 16,
                            colorPrimaryHex = 0xFFD97706,
                            colorSecondaryHex = 0xFFFBBF24
                        )
                    )
                } else {
                    listOf(
                        EnrolledCourse(
                            id = "c10_hum_enrolled_main",
                            title = "এসএসসি মানবিক - ইতিহাস, অর্থনীতি ও পৌরনীতি",
                            badge = "ভর্তি হয়েছো",
                            instructor = "মোশতাক আহমেদ ও টিম",
                            totalClasses = 120,
                            completedClasses = 15,
                            colorPrimaryHex = 0xFF4338CA,
                            colorSecondaryHex = 0xFF6366F1
                        )
                    )
                }
            }

            "C11" -> {
                if (profile.group.contains("বিজ্ঞান") || profile.group.contains("Science")) {
                    listOf(
                        EnrolledCourse(
                            id = "hsc_sci_enrolled_main",
                            title = "HSC বিজ্ঞান - ১ম বর্ষ পূর্ণাঙ্গ প্রস্তুতি",
                            badge = "ভর্তি হয়েছো",
                            instructor = "ড. সাজ্জাদ ও টিম",
                            totalClasses = 160,
                            completedClasses = 6,
                            colorPrimaryHex = 0xFF0284C7,
                            colorSecondaryHex = 0xFF38BDF8
                        )
                    )
                } else if (profile.group.contains("ব্যবসায়") || profile.group.contains("Business")) {
                    listOf(
                        EnrolledCourse(
                            id = "hsc_bus_enrolled_main",
                            title = "HSC ব্যবসায় শিক্ষা - ১ম বর্ষ হিসাববিজ্ঞান ও ফিন্যান্স",
                            badge = "ভর্তি হয়েছো",
                            instructor = "জাভেদ করিম",
                            totalClasses = 120,
                            completedClasses = 5,
                            colorPrimaryHex = 0xFFD97706,
                            colorSecondaryHex = 0xFFFBBF24
                        )
                    )
                } else {
                    // Humanities (Default)
                    listOf(
                        EnrolledCourse(
                            id = "hsc-27-humanities",
                            title = "HSC মানবিক - ১ম বর্ষ প্রস্তুতি",
                            badge = "ভর্তি হয়েছো",
                            instructor = "মোশতাক আহমেদ ও টিম",
                            totalClasses = 140,
                            completedClasses = 2,
                            colorPrimaryHex = 0xFF4338CA,
                            colorSecondaryHex = 0xFF6366F1
                        )
                    )
                }
            }

            "C12" -> {
                if (profile.group.contains("বিজ্ঞান") || profile.group.contains("Science")) {
                    listOf(
                        EnrolledCourse(
                            id = "hsc12_sci_enrolled_main",
                            title = "HSC বিজ্ঞান - ২য় বর্ষ ফাইনাল ও এডমিশন প্রি-প্রিপারেশন",
                            badge = "ভর্তি হয়েছো",
                            instructor = "ড. সাজ্জাদ ও টিম",
                            totalClasses = 175,
                            completedClasses = 20,
                            colorPrimaryHex = 0xFF0284C7,
                            colorSecondaryHex = 0xFF38BDF8
                        )
                    )
                } else if (profile.group.contains("ব্যবসায়") || profile.group.contains("Business")) {
                    listOf(
                        EnrolledCourse(
                            id = "hsc12_bus_enrolled_main",
                            title = "HSC ব্যবসায় শিক্ষা - ২য় বর্ষ হিসাববিজ্ঞান ও ফিন্যান্স",
                            badge = "ভর্তি হয়েছো",
                            instructor = "জাভেদ করিম",
                            totalClasses = 130,
                            completedClasses = 18,
                            colorPrimaryHex = 0xFFD97706,
                            colorSecondaryHex = 0xFFFBBF24
                        )
                    )
                } else {
                    listOf(
                        EnrolledCourse(
                            id = "hsc12_hum_enrolled_main",
                            title = "HSC মানবিক - ২য় বর্ষ প্রস্তুতি",
                            badge = "ভর্তি হয়েছো",
                            instructor = "মোশতাক আহমেদ ও টিম",
                            totalClasses = 140,
                            completedClasses = 15,
                            colorPrimaryHex = 0xFF4338CA,
                            colorSecondaryHex = 0xFF6366F1
                        )
                    )
                }
            }

            else -> listOf(
                EnrolledCourse(
                    id = "admission_enrolled_main",
                    title = "বিশ্ববিদ্যালয় ভর্তি পরীক্ষা পূর্ণাঙ্গ প্রস্তুতি",
                    badge = "ভর্তি হয়েছো",
                    instructor = "শামীম স্যার ও টিম",
                    totalClasses = 120,
                    completedClasses = 10,
                    colorPrimaryHex = 0xFFBE185D,
                    colorSecondaryHex = 0xFFF472B6
                )
            )
        }
    }

    private fun getDefaultClassTag(params: AcademicProgramFilterParams): String {
        val groupBn = when {
            params.group.contains("Science") || params.group.contains("বিজ্ঞান") -> "বিজ্ঞান"
            params.group.contains("Business") || params.group.contains("ব্যবসায়") || params.group.contains("BusinessStudies") -> "ব্যবসায় শিক্ষা"
            params.group.contains("Humanities") || params.group.contains("মানবিক") -> "মানবিক"
            else -> "সাধারণ"
        }
        val batchStr = params.batchId.ifBlank { "HSC 2027" }
        return when (params.className) {
            "C06" -> "ক্লাস ৬ • সাধারণ"
            "C07" -> "ক্লাস ৭ • সাধারণ"
            "C08" -> "ক্লাস ৮ • সাধারণ"
            "C09" -> "ক্লাস ৯ • সাধারণ"
            "C10" -> "এসএসসি • সাধারণ"
            "C11" -> "$batchStr • $groupBn"
            "C12" -> "HSC ২য় বর্ষ • $groupBn"
            "CAD" -> "এডমিশন • ভর্তি প্রস্তুতি"
            else -> "$batchStr • $groupBn"
        }
    }

    /**
     * Returns the complete Academic Programs Catalog (Enrolled, Free, All Courses)
     * strictly segmented for the student's class and syllabus parameters.
     */
    fun getCatalogForParams(params: AcademicProgramFilterParams): AcademicProgramsCatalog {
        val classCode = params.className
        val defaultTag = getDefaultClassTag(params)
        val catalog = when (classCode) {
            "C05" -> AcademicProgramsCatalog(
                enrolledPrograms = listOf(
                    AcademicProgramItem(
                        id = "c05_prog_enrolled_1",
                        title = "ক্লাস ৫ - বার্ষিক ও প্রাথমিক মেধা অন্বেষণ প্রস্তুতি",
                        type = "Paid",
                        hasEnrolment = true,
                        isActive = true,
                        isFree = false,
                        badge = "ভর্তি হয়েছো",
                        phasePricing = 1000
                    )
                ),
                freePrograms = listOf(
                    AcademicProgramItem(
                        id = "c05_prog_free_1",
                        title = "The Next Champ – ক্লাস ৫ প্রাথমিক মেধা অন্বেষণ",
                        type = "Free",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = true,
                        badge = "সম্পূর্ণ ফ্রি!",
                        phasePricing = 0
                    )
                ),
                allCoursesPrograms = listOf(
                    AcademicProgramItem(
                        id = "c05_prog_all_1",
                        title = "ক্লাস ৫ প্রাথমিক গণিত ও বিজ্ঞান স্পেশাল",
                        type = "Paid",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = false,
                        trialEnabled = true,
                        trialDuration = 3,
                        badge = "৩ দিন ফ্রিতে শেখো",
                        phasePricing = 800
                    )
                ),
                blacklistedPrograms = emptyList()
            )

            "C06" -> AcademicProgramsCatalog(
                enrolledPrograms = listOf(
                    AcademicProgramItem(
                        id = "c06_prog_enrolled_1",
                        title = "ক্লাস ৬ - বার্ষিক পরীক্ষার পূর্ণাঙ্গ প্রস্তুতি (২০২৬)",
                        type = "Paid",
                        hasEnrolment = true,
                        isActive = true,
                        isFree = false,
                        badge = "ভর্তি হয়েছো",
                        phasePricing = 1200
                    ),
                    AcademicProgramItem(
                        id = "c06_prog_trial_active",
                        title = "ক্লাস ৬ গণিত ও বিজ্ঞান মাস্টারক্লাস",
                        type = "FullApTrial",
                        hasEnrolment = true,
                        isActive = true,
                        isFree = false,
                        trialEndDate = "2026-09-14",
                        trialDuration = 3,
                        badge = "৩ দিন ফ্রি ট্রায়াল",
                        phasePricing = 850
                    )
                ),
                freePrograms = listOf(
                    AcademicProgramItem(
                        id = "c06_prog_free_1",
                        title = "The Next Champ – ক্লাস ৬ মেধা অন্বেষণ",
                        type = "Free",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = true,
                        badge = "সম্পূর্ণ ফ্রি!",
                        phasePricing = 0
                    ),
                    AcademicProgramItem(
                        id = "c06_prog_free_2",
                        title = "ক্লাস ৬ বেসিক ইংলিশ গ্রামার ও স্পোকেন",
                        type = "Free",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = true,
                        badge = "সম্পূর্ণ ফ্রি!",
                        phasePricing = 0
                    )
                ),
                allCoursesPrograms = listOf(
                    AcademicProgramItem(
                        id = "c06_prog_all_1",
                        title = "ক্লাস ৬ গণিত অলিম্পিয়াড স্পেশাল ব্যাচ",
                        type = "Paid",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = false,
                        trialEnabled = true,
                        trialDuration = 3,
                        badge = "৩ দিন ফ্রিতে শেখো",
                        phasePricing = 950
                    ),
                    AcademicProgramItem(
                        id = "c06_prog_expired",
                        title = "ক্লাস ৬ ফাইনাল রিভিশন ও মডেল টেস্ট",
                        type = "FullApTrial",
                        hasEnrolment = false,
                        isActive = false,
                        isFree = false,
                        trialEndDate = "2025-12-11",
                        badge = "ফ্রিতে শেখা শেষ",
                        phasePricing = 1500
                    ),
                    AcademicProgramItem(
                        id = "c06_prog_all_2",
                        title = "ক্লাস ৬ ডিজিটাল প্রযুক্তি ও আইসিটি প্র্যাকটিক্যাল",
                        type = "Paid",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = false,
                        badge = "বিস্তারিত দেখো",
                        phasePricing = 800
                    )
                ),
                blacklistedPrograms = emptyList()
            )

            "C07" -> AcademicProgramsCatalog(
                enrolledPrograms = listOf(
                    AcademicProgramItem(
                        id = "c07_prog_enrolled_1",
                        title = "ক্লাস ৭ - বার্ষিক পরীক্ষার পূর্ণাঙ্গ প্রস্তুতি (২০২৬)",
                        type = "Paid",
                        hasEnrolment = true,
                        isActive = true,
                        isFree = false,
                        badge = "ভর্তি হয়েছো",
                        phasePricing = 1300
                    )
                ),
                freePrograms = listOf(
                    AcademicProgramItem(
                        id = "c07_prog_free_1",
                        title = "The Next Champ – ক্লাস ৭ মেধা অন্বেষণ",
                        type = "Free",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = true,
                        badge = "সম্পূর্ণ ফ্রি!",
                        phasePricing = 0
                    )
                ),
                allCoursesPrograms = listOf(
                    AcademicProgramItem(
                        id = "c07_prog_all_1",
                        title = "ক্লাস ৭ গণিত ও বিজ্ঞান অ্যাডভান্সড কোর্স",
                        type = "Paid",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = false,
                        trialEnabled = true,
                        trialDuration = 3,
                        badge = "৩ দিন ফ্রিতে শেখো",
                        phasePricing = 990
                    ),
                    AcademicProgramItem(
                        id = "c07_prog_expired",
                        title = "ক্লাস ৭ মডেল টেস্ট ও প্রশ্নব্যাংক সলভ",
                        type = "FullApTrial",
                        hasEnrolment = false,
                        isActive = false,
                        isFree = false,
                        trialEndDate = "2025-12-10",
                        badge = "ফ্রিতে শেখা শেষ",
                        phasePricing = 1400
                    )
                ),
                blacklistedPrograms = emptyList()
            )

            "C08" -> AcademicProgramsCatalog(
                enrolledPrograms = listOf(
                    AcademicProgramItem(
                        id = "c08_prog_enrolled_1",
                        title = "ক্লাস ৮ - বার্ষিক পরীক্ষা ও জেএসসি ফাউন্ডেশন প্রস্তুতি",
                        type = "Paid",
                        hasEnrolment = true,
                        isActive = true,
                        isFree = false,
                        badge = "ভর্তি হয়েছো",
                        phasePricing = 1400
                    )
                ),
                freePrograms = listOf(
                    AcademicProgramItem(
                        id = "c08_prog_free_1",
                        title = "The Next Champ – ক্লাস ৮ মেধা অন্বেষণ",
                        type = "Free",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = true,
                        badge = "সম্পূর্ণ ফ্রি!",
                        phasePricing = 0
                    )
                ),
                allCoursesPrograms = listOf(
                    AcademicProgramItem(
                        id = "c08_prog_all_1",
                        title = "ক্লাস ৮ সাধারণ গণিত ও বিজ্ঞান মাস্টারকোর্স",
                        type = "Paid",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = false,
                        trialEnabled = true,
                        trialDuration = 3,
                        badge = "৩ দিন ফ্রিতে শেখো",
                        phasePricing = 1100
                    ),
                    AcademicProgramItem(
                        id = "c08_prog_expired",
                        title = "ক্লাস ৮ রিভিশন ও পূর্ণাঙ্গ মডেল টেস্ট",
                        type = "FullApTrial",
                        hasEnrolment = false,
                        isActive = false,
                        isFree = false,
                        trialEndDate = "2025-12-05",
                        badge = "ফ্রিতে শেখা শেষ",
                        phasePricing = 1500
                    )
                ),
                blacklistedPrograms = emptyList()
            )

            "C09" -> {
                if (params.group.contains("Science")) {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(AcademicProgramItem(id = "c09_sci_enrolled", title = "ক্লাস ৯ বিজ্ঞান - গণিত ও পদার্থবিজ্ঞান ফাউন্ডেশন", type = "Paid", hasEnrolment = true, isActive = true, phasePricing = 1600)),
                        freePrograms = listOf(AcademicProgramItem(id = "c09_sci_free", title = "ক্লাস ৯ বিজ্ঞান মেধা অন্বেষণ", type = "Free", hasEnrolment = false, isActive = true, isFree = true, phasePricing = 0)),
                        allCoursesPrograms = listOf(AcademicProgramItem(id = "c09_sci_all", title = "ক্লাস ৯ উচ্চতর গণিত ও রসায়ন মাস্টারক্লাস", type = "Paid", hasEnrolment = false, isActive = true, trialEnabled = true, trialDuration = 3, phasePricing = 1200)),
                        blacklistedPrograms = emptyList()
                    )
                } else if (params.group.contains("Business") || params.group.contains("ব্যবসায়")) {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(AcademicProgramItem(id = "c09_bus_enrolled", title = "ক্লাস ৯ ব্যবসায় শিক্ষা - হিসাববিজ্ঞান বেসিক", type = "Paid", hasEnrolment = true, isActive = true, phasePricing = 1500)),
                        freePrograms = listOf(AcademicProgramItem(id = "c09_bus_free", title = "ক্লাস ৯ ফিন্যান্স ও ব্যাংকিং পরিচিতি", type = "Free", hasEnrolment = false, isActive = true, isFree = true, phasePricing = 0)),
                        allCoursesPrograms = listOf(AcademicProgramItem(id = "c09_bus_all", title = "ক্লাস ৯ উদ্যোক্তা উন্নয়ন ও হিসাববিজ্ঞান", type = "Paid", hasEnrolment = false, isActive = true, trialEnabled = true, trialDuration = 3, phasePricing = 1100)),
                        blacklistedPrograms = emptyList()
                    )
                } else {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(AcademicProgramItem(id = "c09_hum_enrolled", title = "ক্লাস ৯ মানবিক - ইতিহাস ও ভূগোল ফাউন্ডেশন", type = "Paid", hasEnrolment = true, isActive = true, phasePricing = 1400)),
                        freePrograms = listOf(AcademicProgramItem(id = "c09_hum_free", title = "ক্লাস ৯ বাংলাদেশ ও বিশ্বপরিচয়", type = "Free", hasEnrolment = false, isActive = true, isFree = true, phasePricing = 0)),
                        allCoursesPrograms = listOf(AcademicProgramItem(id = "c09_hum_all", title = "ক্লাস ৯ পৌরনীতি ও নাগরিকতা স্পেশাল", type = "Paid", hasEnrolment = false, isActive = true, trialEnabled = true, trialDuration = 3, phasePricing = 1000)),
                        blacklistedPrograms = emptyList()
                    )
                }
            }

            "C10" -> {
                if (params.group.contains("Science")) {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(AcademicProgramItem(id = "c10_sci_enrolled", title = "এসএসসি বিজ্ঞান - পূর্ণাঙ্গ রিভিশন ও টেস্ট পেপার সলভ", type = "Paid", hasEnrolment = true, isActive = true, phasePricing = 2200)),
                        freePrograms = listOf(AcademicProgramItem(id = "c10_sci_free", title = "এসএসসি বিজ্ঞান মেধা অন্বেষণ চ্যালেঞ্জ", type = "Free", hasEnrolment = false, isActive = true, isFree = true, phasePricing = 0)),
                        allCoursesPrograms = listOf(AcademicProgramItem(id = "c10_sci_all", title = "এসএসসি পদার্থবিজ্ঞান, রসায়ন ও উচ্চতর গণিত ফাইনাল", type = "Paid", hasEnrolment = false, isActive = true, trialEnabled = true, trialDuration = 3, phasePricing = 1450)),
                        blacklistedPrograms = emptyList()
                    )
                } else if (params.group.contains("Business") || params.group.contains("ব্যবসায়")) {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(AcademicProgramItem(id = "c10_bus_enrolled", title = "এসএসসি ব্যবসায় শিক্ষা - হিসাববিজ্ঞান ও ফিন্যান্স", type = "Paid", hasEnrolment = true, isActive = true, phasePricing = 2000)),
                        freePrograms = listOf(AcademicProgramItem(id = "c10_bus_free", title = "এসএসসি ব্যবসায় উদ্যোগ ফর্মুলা শিট", type = "Free", hasEnrolment = false, isActive = true, isFree = true, phasePricing = 0)),
                        allCoursesPrograms = listOf(AcademicProgramItem(id = "c10_bus_all", title = "এসএসসি হিসাববিজ্ঞান টেস্ট পেপার সলভ", type = "Paid", hasEnrolment = false, isActive = true, trialEnabled = true, trialDuration = 3, phasePricing = 1300)),
                        blacklistedPrograms = emptyList()
                    )
                } else {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(AcademicProgramItem(id = "c10_hum_enrolled", title = "এসএসসি মানবিক - ইতিহাস, অর্থনীতি ও পৌরনীতি", type = "Paid", hasEnrolment = true, isActive = true, phasePricing = 1900)),
                        freePrograms = listOf(AcademicProgramItem(id = "c10_hum_free", title = "এসএসসি মানবিক বিষয়ভিত্তিক শর্টকাট সাজেশন", type = "Free", hasEnrolment = false, isActive = true, isFree = true, phasePricing = 0)),
                        allCoursesPrograms = listOf(AcademicProgramItem(id = "c10_hum_all", title = "এসএসসি ভূগোল ও পরিবেশ স্পেশাল কোর্স", type = "Paid", hasEnrolment = false, isActive = true, trialEnabled = true, trialDuration = 3, phasePricing = 1200)),
                        blacklistedPrograms = emptyList()
                    )
                }
            }

            "C11" -> {
                if (params.group.contains("Science")) {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(
                            AcademicProgramItem(
                                id = "hsc_sci_enrolled_1",
                                title = "HSC বিজ্ঞান - ১ম বর্ষ পূর্ণাঙ্গ প্রস্তুতি",
                                type = "Paid",
                                hasEnrolment = true,
                                isActive = true,
                                isFree = false,
                                badge = "ভর্তি হয়েছো",
                                phasePricing = 2800
                            )
                        ),
                        freePrograms = listOf(
                            AcademicProgramItem(
                                id = "hsc_sci_free_1",
                                title = "The Next Champ – HSC Science",
                                type = "Free",
                                hasEnrolment = false,
                                isActive = true,
                                isFree = true,
                                badge = "সম্পূর্ণ ফ্রি!",
                                phasePricing = 0
                            )
                        ),
                        allCoursesPrograms = listOf(
                            AcademicProgramItem(
                                id = "hsc_sci_all_1",
                                title = "HSC উচ্চতর গণিত ও পদার্থবিজ্ঞান স্পেশাল",
                                type = "Paid",
                                hasEnrolment = false,
                                isActive = true,
                                isFree = false,
                                trialEnabled = true,
                                trialDuration = 3,
                                badge = "৩ দিন ফ্রিতে শেখো",
                                phasePricing = 1600
                            )
                        ),
                        blacklistedPrograms = emptyList()
                    )
                } else if (params.group.contains("Business") || params.group.contains("ব্যবসায়") || params.group.contains("BusinessStudies")) {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(
                            AcademicProgramItem(
                                id = "hsc_bus_enrolled_1",
                                title = "HSC ব্যবসায় শিক্ষা - হিসাববিজ্ঞান ও ফিন্যান্স",
                                type = "Paid",
                                hasEnrolment = true,
                                isActive = true,
                                isFree = false,
                                badge = "ভর্তি হয়েছো",
                                phasePricing = 2400
                            )
                        ),
                        freePrograms = listOf(
                            AcademicProgramItem(
                                id = "hsc_bus_free_1",
                                title = "The Next Champ – HSC Business Studies",
                                type = "Free",
                                hasEnrolment = false,
                                isActive = true,
                                isFree = true,
                                badge = "সম্পূর্ণ ফ্রি!",
                                phasePricing = 0
                            )
                        ),
                        allCoursesPrograms = listOf(
                            AcademicProgramItem(
                                id = "hsc_bus_all_1",
                                title = "ব্যবসায় সংগঠন ও ব্যবস্থাপনা স্পেশাল কোর্স",
                                type = "Paid",
                                hasEnrolment = false,
                                isActive = true,
                                isFree = false,
                                trialEnabled = true,
                                trialDuration = 3,
                                badge = "৩ দিন ফ্রিতে শেখো",
                                phasePricing = 1350
                            )
                        ),
                        blacklistedPrograms = emptyList()
                    )
                } else {
                    // HSC Humanities (Default)
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(
                            AcademicProgramItem(
                                id = "6864d3a806800acba2e27099",
                                title = "HSC মানবিক - ১ম ও ২য় বর্ষ প্রস্তুতি",
                                type = "Paid",
                                hasEnrolment = true,
                                isActive = true,
                                isFree = false,
                                badge = "ভর্তি হয়েছো",
                                phasePricing = 2500
                            ),
                            AcademicProgramItem(
                                id = "think_ai_prog_1",
                                title = "Think AI - স্মার্ট স্টাডি অ্যাসিস্ট্যান্ট",
                                type = "Paid",
                                hasEnrolment = true,
                                isActive = true,
                                isFree = false,
                                expiryDate = "2029-01-01",
                                badge = "ভর্তি হয়েছো",
                                phasePricing = 1000
                            )
                        ),
                        freePrograms = listOf(
                            AcademicProgramItem(
                                id = "the_next_champ_hsc27",
                                title = "The Next Champ – HSC",
                                type = "Free",
                                hasEnrolment = false,
                                isActive = true,
                                isFree = true,
                                badge = "সম্পূর্ণ ফ্রি!",
                                phasePricing = 0
                            ),
                            AcademicProgramItem(
                                id = "hsc_bangla_grammar_free",
                                title = "HSC বাংলা ব্যাকরণ ও নির্মিতি ফাউন্ডেশন",
                                type = "Free",
                                hasEnrolment = false,
                                isActive = true,
                                isFree = true,
                                badge = "সম্পূর্ণ ফ্রি!",
                                phasePricing = 0
                            )
                        ),
                        allCoursesPrograms = listOf(
                            AcademicProgramItem(
                                id = "duronto_hsc28_hum",
                                title = "দুরন্ত HSC মানবিক মাস্টারক্লাস",
                                type = "Paid",
                                hasEnrolment = false,
                                isActive = true,
                                isFree = false,
                                trialEnabled = true,
                                trialDuration = 3,
                                badge = "৩ দিন ফ্রিতে শেখো",
                                phasePricing = 2950
                            ),
                            AcademicProgramItem(
                                id = "hsc_ict_masterclass",
                                title = "HSC তথ্য ও যোগাযোগ প্রযুক্তি (ICT) সম্পূর্ণ কোর্স",
                                type = "Paid",
                                hasEnrolment = false,
                                isActive = true,
                                isFree = false,
                                badge = "বিস্তারিত দেখো",
                                phasePricing = 1200
                            )
                        ),
                        blacklistedPrograms = emptyList()
                    )
                }
            }

            "C12" -> {
                if (params.group.contains("Science")) {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(AcademicProgramItem(id = "hsc12_sci_enrolled", title = "HSC বিজ্ঞান - ২য় বর্ষ ফাইনাল ও এডমিশন প্রি-প্রিপারেশন", type = "Paid", hasEnrolment = true, isActive = true, phasePricing = 3000)),
                        freePrograms = listOf(AcademicProgramItem(id = "hsc12_sci_free", title = "HSC ২য় বর্ষ ফিজিক্স ও কেমিস্ট্রি সাজেশন", type = "Free", hasEnrolment = false, isActive = true, isFree = true, phasePricing = 0)),
                        allCoursesPrograms = listOf(AcademicProgramItem(id = "hsc12_sci_all", title = "HSC ২য় বর্ষ টেস্ট পেপার সলভ - বিজ্ঞান", type = "Paid", hasEnrolment = false, isActive = true, trialEnabled = true, trialDuration = 3, phasePricing = 1800)),
                        blacklistedPrograms = emptyList()
                    )
                } else if (params.group.contains("Business") || params.group.contains("ব্যবসায়")) {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(AcademicProgramItem(id = "hsc12_bus_enrolled", title = "HSC ব্যবসায় শিক্ষা - ২য় বর্ষ হিসাববিজ্ঞান ও ফিন্যান্স", type = "Paid", hasEnrolment = true, isActive = true, phasePricing = 2600)),
                        freePrograms = listOf(AcademicProgramItem(id = "hsc12_bus_free", title = "HSC ব্যবসায় শিক্ষা শর্ট সাজেশন", type = "Free", hasEnrolment = false, isActive = true, isFree = true, phasePricing = 0)),
                        allCoursesPrograms = listOf(AcademicProgramItem(id = "hsc12_bus_all", title = "HSC ২য় বর্ষ ফিন্যান্স ও ব্যাংকিং স্পেশাল", type = "Paid", hasEnrolment = false, isActive = true, trialEnabled = true, trialDuration = 3, phasePricing = 1500)),
                        blacklistedPrograms = emptyList()
                    )
                } else {
                    AcademicProgramsCatalog(
                        enrolledPrograms = listOf(AcademicProgramItem(id = "hsc12_hum_enrolled", title = "HSC মানবিক - ২য় বর্ষ ফাইনাল প্রস্তুতি", type = "Paid", hasEnrolment = true, isActive = true, phasePricing = 2500)),
                        freePrograms = listOf(AcademicProgramItem(id = "hsc12_hum_free", title = "HSC মানবিক ২য় বর্ষ মডেল টেস্ট", type = "Free", hasEnrolment = false, isActive = true, isFree = true, phasePricing = 0)),
                        allCoursesPrograms = listOf(AcademicProgramItem(id = "hsc12_hum_all", title = "HSC পৌরনীতি, অর্থনীতি ও সমাজবিজ্ঞান ২য় পত্র", type = "Paid", hasEnrolment = false, isActive = true, trialEnabled = true, trialDuration = 3, phasePricing = 1400)),
                        blacklistedPrograms = emptyList()
                    )
                }
            }

            "CAD" -> AcademicProgramsCatalog(
                enrolledPrograms = listOf(
                    AcademicProgramItem(
                        id = "admission_enrolled_1",
                        title = "বিশ্ববিদ্যালয় ভর্তি পরীক্ষা 'খ' ও 'গ' ইউনিট পূর্ণাঙ্গ প্রস্তুতি",
                        type = "Paid",
                        hasEnrolment = true,
                        isActive = true,
                        isFree = false,
                        badge = "ভর্তি হয়েছো",
                        phasePricing = 3500
                    )
                ),
                freePrograms = listOf(
                    AcademicProgramItem(
                        id = "admission_free_1",
                        title = "এডমিশন জিকে ও সাম্প্রতিক তথ্য মেগা সিরিজ",
                        type = "Free",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = true,
                        badge = "সম্পূর্ণ ফ্রি!",
                        phasePricing = 0
                    )
                ),
                allCoursesPrograms = listOf(
                    AcademicProgramItem(
                        id = "admission_all_1",
                        title = "মেডিকেল ও ইঞ্জিনিয়ারিং ভর্তি প্রস্তুতি স্পেশাল মডেল টেস্ট",
                        type = "Paid",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = false,
                        trialEnabled = true,
                        trialDuration = 3,
                        badge = "৩ দিন ফ্রিতে শেখো",
                        phasePricing = 2500
                    )
                ),
                blacklistedPrograms = emptyList()
            )

            else -> AcademicProgramsCatalog(
                enrolledPrograms = listOf(
                    AcademicProgramItem(
                        id = "default_enrolled_1",
                        title = "পূর্ণাঙ্গ একাডেমিক ও স্কলারশিপ প্রস্তুতি কোর্স",
                        type = "Paid",
                        hasEnrolment = true,
                        isActive = true,
                        isFree = false,
                        badge = "ভর্তি হয়েছো",
                        phasePricing = 2500
                    )
                ),
                freePrograms = listOf(
                    AcademicProgramItem(
                        id = "default_free_1",
                        title = "ফ্রি স্কলারশিপ ও মেধা অন্বেষণ কোর্স",
                        type = "Free",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = true,
                        badge = "সম্পূর্ণ ফ্রি!",
                        phasePricing = 0
                    )
                ),
                allCoursesPrograms = listOf(
                    AcademicProgramItem(
                        id = "default_all_1",
                        title = "মাস্টারক্লাস ও স্পেশাল স্কিল ডেভেলপমেন্ট",
                        type = "Paid",
                        hasEnrolment = false,
                        isActive = true,
                        isFree = false,
                        trialEnabled = true,
                        trialDuration = 3,
                        badge = "৩ দিন ফ্রিতে শেখো",
                        phasePricing = 1500
                    )
                ),
                blacklistedPrograms = emptyList()
            )
        }
        return catalog.copy(
            enrolledPrograms = catalog.enrolledPrograms.map { it.copy(classTag = defaultTag) },
            freePrograms = catalog.freePrograms.map { it.copy(classTag = defaultTag) },
            allCoursesPrograms = catalog.allCoursesPrograms.map { it.copy(classTag = defaultTag) },
            blacklistedPrograms = catalog.blacklistedPrograms
        )
    }

    /**
     * Generates all Subjects for the Explore screen and Quarter Detail screen
     * strictly mapped to the student's class and group.
     */
    fun getSubjectsForProfile(profile: UserProfile): List<SubjectItem> {
        val classCode = getClassCode(profile.studentClass)
        return when (classCode) {
            "C05" -> listOf(
                SubjectItem("sub_c05_bangla", "বাংলা (আমার বাংলা বই)", "বাং", 0xFFE11D48, 0xFFF43F5E, 20),
                SubjectItem("sub_c05_english", "English For Today", "Eng", 0xFF2563EB, 0xFF3B82F6, 25),
                SubjectItem("sub_c05_math", "প্রাথমিক গণিত", "গণি", 0xFF059669, 0xFF10B981, 35),
                SubjectItem("sub_c05_science", "প্রাথমিক বিজ্ঞান", "বিজ্ঞা", 0xFF7C3AED, 0xFF8B5CF6, 20),
                SubjectItem("sub_c05_bgs", "বাংলাদেশ ও বিশ্বপরিচয়", "বাওবি", 0xFFD97706, 0xFFF59E0B, 15),
                SubjectItem("sub_c05_religion", "ধর্ম ও নৈতিক শিক্ষা", "ধর্ম", 0xFF4F46E5, 0xFF6366F1, 20)
            )

            "C06" -> listOf(
                SubjectItem("sub_c06_bangla", "বাংলা (সাহিত্য ও ব্যাকরণ)", "বাং", 0xFFE11D48, 0xFFF43F5E, 25),
                SubjectItem("sub_c06_english", "ইংরেজি (English For Today)", "Eng", 0xFF2563EB, 0xFF3B82F6, 30),
                SubjectItem("sub_c06_math", "গণিত (Mathematics)", "গণি", 0xFF059669, 0xFF10B981, 40),
                SubjectItem("sub_c06_science", "বিজ্ঞান (অনুসন্ধানী ও অনুশীলন)", "বিজ্ঞা", 0xFF7C3AED, 0xFF8B5CF6, 20),
                SubjectItem("sub_c06_ict", "ডিজিটাল প্রযুক্তি (ICT)", "ICT", 0xFF0284C7, 0xFF0EA5E9, 50),
                SubjectItem("sub_c06_history", "ইতিহাস ও সামাজিক বিজ্ঞান", "ইতি", 0xFFD97706, 0xFFF59E0B, 15),
                SubjectItem("sub_c06_religion", "ধর্ম ও নৈতিক শিক্ষা", "ধর্ম", 0xFF4F46E5, 0xFF6366F1, 35),
                SubjectItem("sub_c06_life", "স্বাস্থ্য সুরক্ষা ও জীবন-জীবিকা", "স্বাস্থ", 0xFF0D9488, 0xFF14B8A6, 10),
                SubjectItem("sub_c06_art", "শিল্প ও সংস্কৃতি", "শিল্প", 0xFFBE185D, 0xFFEC4899, 18)
            )

            "C07" -> listOf(
                SubjectItem("sub_c07_bangla", "বাংলা ১ম ও ২য় পত্র", "বাং", 0xFFE11D48, 0xFFF43F5E, 22),
                SubjectItem("sub_c07_english", "English For Today & Grammar", "Eng", 0xFF2563EB, 0xFF3B82F6, 28),
                SubjectItem("sub_c07_math", "গণিত", "গণি", 0xFF059669, 0xFF10B981, 35),
                SubjectItem("sub_c07_science", "বিজ্ঞান", "বিজ্ঞা", 0xFF7C3AED, 0xFF8B5CF6, 18),
                SubjectItem("sub_c07_ict", "ডিজিটাল প্রযুক্তি", "ICT", 0xFF0284C7, 0xFF0EA5E9, 45),
                SubjectItem("sub_c07_history", "ইতিহাস ও সামাজিক বিজ্ঞান", "ইতি", 0xFFD97706, 0xFFF59E0B, 12),
                SubjectItem("sub_c07_religion", "ধর্ম ও নৈতিক শিক্ষা", "ধর্ম", 0xFF4F46E5, 0xFF6366F1, 30)
            )

            "C08" -> listOf(
                SubjectItem("sub_c08_bangla", "বাংলা ১ম ও ২য় পত্র", "বাং", 0xFFE11D48, 0xFFF43F5E, 20),
                SubjectItem("sub_c08_english", "English", "Eng", 0xFF2563EB, 0xFF3B82F6, 25),
                SubjectItem("sub_c08_math", "গণিত", "গণি", 0xFF059669, 0xFF10B981, 32),
                SubjectItem("sub_c08_science", "বিজ্ঞান", "বিজ্ঞা", 0xFF7C3AED, 0xFF8B5CF6, 15),
                SubjectItem("sub_c08_ict", "তথ্য ও যোগাযোগ প্রযুক্তি", "ICT", 0xFF0284C7, 0xFF0EA5E9, 40),
                SubjectItem("sub_c08_bgs", "বাংলাদেশ ও বিশ্বপরিচয়", "বাওবি", 0xFFD97706, 0xFFF59E0B, 10),
                SubjectItem("sub_c08_religion", "ধর্ম শিক্ষা", "ধর্ম", 0xFF4F46E5, 0xFF6366F1, 28)
            )

            "C09", "C10" -> {
                if (profile.group.contains("বিজ্ঞান") || profile.group.contains("Science")) {
                    listOf(
                        SubjectItem("sub_ssc_bangla", "বাংলা ১ম ও ২য় পত্র", "বাং", 0xFFE11D48, 0xFFF43F5E, 45),
                        SubjectItem("sub_ssc_english", "English 1st & 2nd Paper", "Eng", 0xFF2563EB, 0xFF3B82F6, 50),
                        SubjectItem("sub_ssc_math", "সাধারণ গণিত", "গণি", 0xFF059669, 0xFF10B981, 60),
                        SubjectItem("sub_ssc_hmath", "উচ্চতর গণিত", "উগণি", 0xFF0D9488, 0xFF14B8A6, 40),
                        SubjectItem("sub_ssc_physics", "পদার্থবিজ্ঞান", "পদ", 0xFF7C3AED, 0xFF8B5CF6, 38),
                        SubjectItem("sub_ssc_chemistry", "রসায়ন", "রস", 0xFFD97706, 0xFFF59E0B, 42),
                        SubjectItem("sub_ssc_biology", "জীববিজ্ঞান", "জীব", 0xFF059669, 0xFF34D399, 35),
                        SubjectItem("sub_ssc_bgs", "বাংলাদেশ ও বিশ্বপরিচয়", "বাওবি", 0xFFBE185D, 0xFFEC4899, 25),
                        SubjectItem("sub_ssc_ict", "তথ্য ও যোগাযোগ প্রযুক্তি (ICT)", "ICT", 0xFF0284C7, 0xFF0EA5E9, 55)
                    )
                } else if (profile.group.contains("ব্যবসায়") || profile.group.contains("Business")) {
                    listOf(
                        SubjectItem("sub_ssc_bus_ban", "বাংলা ১ম ও ২য় পত্র", "বাং", 0xFFE11D48, 0xFFF43F5E, 40),
                        SubjectItem("sub_ssc_bus_eng", "English 1st & 2nd Paper", "Eng", 0xFF2563EB, 0xFF3B82F6, 45),
                        SubjectItem("sub_ssc_bus_math", "সাধারণ গণিত", "গণি", 0xFF059669, 0xFF10B981, 50),
                        SubjectItem("sub_ssc_bus_acc", "হিসাববিজ্ঞান", "হিসা", 0xFF0D9488, 0xFF14B8A6, 45),
                        SubjectItem("sub_ssc_bus_fin", "ফিন্যান্স ও ব্যাংকিং", "ফিন্যা", 0xFFD97706, 0xFFF59E0B, 35),
                        SubjectItem("sub_ssc_bus_ent", "ব্যবসায় উদ্যোগ", "ব্যব", 0xFF7C3AED, 0xFF8B5CF6, 30),
                        SubjectItem("sub_ssc_bus_sci", "সাধারণ বিজ্ঞান", "বিজ্ঞা", 0xFF059669, 0xFF34D399, 25),
                        SubjectItem("sub_ssc_bus_bgs", "বাংলাদেশ ও বিশ্বপরিচয়", "বাওবি", 0xFFBE185D, 0xFFEC4899, 20),
                        SubjectItem("sub_ssc_bus_ict", "তথ্য ও যোগাযোগ প্রযুক্তি (ICT)", "ICT", 0xFF0284C7, 0xFF0EA5E9, 50)
                    )
                } else {
                    listOf(
                        SubjectItem("sub_ssc_hum_ban", "বাংলা ১ম ও ২য় পত্র", "বাং", 0xFFE11D48, 0xFFF43F5E, 40),
                        SubjectItem("sub_ssc_hum_eng", "English 1st & 2nd Paper", "Eng", 0xFF2563EB, 0xFF3B82F6, 45),
                        SubjectItem("sub_ssc_hum_math", "সাধারণ গণিত", "গণি", 0xFF059669, 0xFF10B981, 50),
                        SubjectItem("sub_ssc_hum_his", "বাংলাদেশের ইতিহাস ও বিশ্বসভ্যতা", "ইতি", 0xFFD97706, 0xFFF59E0B, 35),
                        SubjectItem("sub_ssc_hum_geo", "ভূগোল ও পরিবেশ", "ভূগোল", 0xFF0D9488, 0xFF14B8A6, 30),
                        SubjectItem("sub_ssc_hum_civ", "পৌরনীতি ও নাগরিকতা", "পৌর", 0xFF7C3AED, 0xFF8B5CF6, 30),
                        SubjectItem("sub_ssc_hum_eco", "অর্থনীতি", "অর্থ", 0xFFBE185D, 0xFFEC4899, 25),
                        SubjectItem("sub_ssc_hum_sci", "সাধারণ বিজ্ঞান", "বিজ্ঞা", 0xFF059669, 0xFF34D399, 25),
                        SubjectItem("sub_ssc_hum_ict", "তথ্য ও যোগাযোগ প্রযুক্তি (ICT)", "ICT", 0xFF0284C7, 0xFF0EA5E9, 50)
                    )
                }
            }

            "C11", "C12" -> {
                if (profile.group.contains("বিজ্ঞান") || profile.group.contains("Science")) {
                    listOf(
                        SubjectItem("sub_hsc_phy", "পদার্থবিজ্ঞান ১ম ও ২য় পত্র", "পদ", 0xFF7C3AED, 0xFF8B5CF6, 30),
                        SubjectItem("sub_hsc_chem", "রসায়ন ১ম ও ২য় পত্র", "রস", 0xFFD97706, 0xFFF59E0B, 28),
                        SubjectItem("sub_hsc_hmath", "উচ্চতর গণিত ১ম ও ২য় পত্র", "উগণি", 0xFF059669, 0xFF10B981, 35),
                        SubjectItem("sub_hsc_bio", "জীববিজ্ঞান ১ম ও ২য় পত্র", "জীব", 0xFF0D9488, 0xFF14B8A6, 22),
                        SubjectItem("sub_hsc_ict", "তথ্য ও যোগাযোগ প্রযুক্তি (ICT)", "ICT", 0xFF0284C7, 0xFF0EA5E9, 45),
                        SubjectItem("sub_hsc_ban", "বাংলা ১ম ও ২য় পত্র", "বাং", 0xFFE11D48, 0xFFF43F5E, 20),
                        SubjectItem("sub_hsc_eng", "English 1st & 2nd Paper", "Eng", 0xFF2563EB, 0xFF3B82F6, 25)
                    )
                } else if (profile.group.contains("ব্যবসায়") || profile.group.contains("Business")) {
                    listOf(
                        SubjectItem("sub_hsc_acc", "হিসাববিজ্ঞান ১ম ও ২য় পত্র", "হিসা", 0xFF059669, 0xFF10B981, 30),
                        SubjectItem("sub_hsc_fin", "ফিন্যান্স, ব্যাংকিং ও বীমা", "ফিন্যা", 0xFFD97706, 0xFFF59E0B, 25),
                        SubjectItem("sub_hsc_mgmt", "ব্যবসায় সংগঠন ও ব্যবস্থাপনা", "ব্যব", 0xFF7C3AED, 0xFF8B5CF6, 22),
                        SubjectItem("sub_hsc_ict", "তথ্য ও যোগাযোগ প্রযুক্তি (ICT)", "ICT", 0xFF0284C7, 0xFF0EA5E9, 40),
                        SubjectItem("sub_hsc_ban", "বাংলা ১ম ও ২য় পত্র", "বাং", 0xFFE11D48, 0xFFF43F5E, 20),
                        SubjectItem("sub_hsc_eng", "English 1st & 2nd Paper", "Eng", 0xFF2563EB, 0xFF3B82F6, 25)
                    )
                } else {
                    // Humanities (Default)
                    listOf(
                        SubjectItem("sub_hsc_hum_ban", "বাংলা ১ম ও ২য় পত্র", "বাং", 0xFFE11D48, 0xFFF43F5E, 24),
                        SubjectItem("sub_hsc_hum_eng", "English 1st & 2nd Paper", "Eng", 0xFF2563EB, 0xFF3B82F6, 30),
                        SubjectItem("sub_hsc_hum_ict", "তথ্য ও যোগাযোগ প্রযুক্তি (ICT)", "ICT", 0xFF0284C7, 0xFF0EA5E9, 50),
                        SubjectItem("sub_hsc_hum_eco", "অর্থনীতি ১ম ও ২য় পত্র", "অর্থ", 0xFFD97706, 0xFFF59E0B, 15),
                        SubjectItem("sub_hsc_hum_civ", "পৌরনীতি ও সুশাসন", "পৌর", 0xFF7C3AED, 0xFF8B5CF6, 18),
                        SubjectItem("sub_hsc_hum_soc", "সমাজবিজ্ঞান", "সমাজ", 0xFF059669, 0xFF10B981, 12),
                        SubjectItem("sub_hsc_hum_his", "ইসলামের ইতিহাস ও সংস্কৃতি", "ইস্হা", 0xFF0D9488, 0xFF14B8A6, 20),
                        SubjectItem("sub_hsc_hum_log", "যুক্তিবিদ্যা", "যুক্তি", 0xFFBE185D, 0xFFEC4899, 10)
                    )
                }
            }

            else -> listOf(
                SubjectItem("sub_adm_ban", "বাংলা সাহিত্য ও ব্যাকরণ", "বাং", 0xFFE11D48, 0xFFF43F5E, 40),
                SubjectItem("sub_adm_eng", "English Vocabulary & Grammar", "Eng", 0xFF2563EB, 0xFF3B82F6, 45),
                SubjectItem("sub_adm_gk", "সাধারণ জ্ঞান (বাংলাদেশ ও আন্তর্জাতিক)", "GK", 0xFFD97706, 0xFFF59E0B, 50),
                SubjectItem("sub_adm_iq", "আইকিউ ও বিশ্লেষণমূলক দক্ষতা", "IQ", 0xFF7C3AED, 0xFF8B5CF6, 35)
            )
        }
    }

    /**
     * Generates 7-day routine tailored to the student's class and subjects
     */
    fun getWeeklyRoutineForProfile(profile: UserProfile): List<DaySchedule> {
        val classCode = getClassCode(profile.studentClass)
        val dayNames = listOf(
            Pair("শনি", "শনিবার"),
            Pair("রবি", "রবিবার"),
            Pair("সোম", "সোমবার"),
            Pair("মঙ্গল", "মঙ্গলবার"),
            Pair("বুধ", "বুধবার"),
            Pair("বৃহঃ", "বৃহস্পতিবার"),
            Pair("শুক্র", "শুক্রবার")
        )
        val dates = listOf("০৫", "০৬", "০৭", "০৮", "০৯", "১০", "১১")

        return dayNames.mapIndexed { index, (shortName, fullName) ->
            val classes = when (classCode) {
                "C06" -> when (index) {
                    0 -> listOf(
                        RoutineClassItem(
                            id = "c06_c_01",
                            time = "সন্ধ্যা ০৬:০০",
                            durationText = "১ ঘণ্টা",
                            subject = "গণিত",
                            chapterOrTopic = "সংখ্যা ও দ্বিমাত্রিক বস্তু - অনুশীলন",
                            instructor = "মোজাম্মেল হক",
                            typeLabel = "লাইভ ক্লাস",
                            type = RoutineItemType.LIVE_CLASS,
                            subjectColorHex = 0xFF059669,
                            isLiveNow = false
                        )
                    )
                    2 -> listOf(
                        RoutineClassItem(
                            id = "c06_c_02",
                            time = "সন্ধ্যা ০৭:০০",
                            durationText = "১ ঘণ্টা",
                            subject = "বিজ্ঞান",
                            chapterOrTopic = "গতি ও বল - অনুসন্ধানী পাঠ",
                            instructor = "ড. নাসিমা আক্তার",
                            typeLabel = "লাইভ ক্লাস",
                            type = RoutineItemType.LIVE_CLASS,
                            subjectColorHex = 0xFF7C3AED,
                            isLiveNow = false
                        )
                    )
                    4 -> listOf(
                        RoutineClassItem(
                            id = "c06_c_03",
                            time = "রাত ০৮:০০",
                            durationText = "১ ঘণ্টা ১৫ মিনিট",
                            subject = "ডিজিটাল প্রযুক্তি",
                            chapterOrTopic = "সাইবার নিরাপত্তা ও নেটওয়ার্ক পরিচিতি",
                            instructor = "রাকিব হাসান",
                            typeLabel = "লাইভ ক্লাস",
                            type = RoutineItemType.LIVE_CLASS,
                            subjectColorHex = 0xFF0284C7,
                            isLiveNow = true
                        )
                    )
                    else -> emptyList()
                }

                "C10" -> when (index) {
                    0 -> listOf(
                        RoutineClassItem(
                            id = "c10_c_01",
                            time = "সন্ধ্যা ০৭:০০",
                            durationText = "১ ঘণ্টা ৩০ মিনিট",
                            subject = "উচ্চতর গণিত",
                            chapterOrTopic = "ত্রিকোণমিতি ও স্থানাঙ্ক জ্যামিতি স্পেশাল",
                            instructor = "কবির হোসেন",
                            typeLabel = "লাইভ ক্লাস",
                            type = RoutineItemType.LIVE_CLASS,
                            subjectColorHex = 0xFF0D9488,
                            isLiveNow = false
                        )
                    )
                    4 -> listOf(
                        RoutineClassItem(
                            id = "c10_c_02",
                            time = "রাত ০৮:০০",
                            durationText = "১ ঘণ্টা ৩০ মিনিট",
                            subject = "পদার্থবিজ্ঞান",
                            chapterOrTopic = "আলোর প্রতিফলন ও প্রতিসরণ",
                            instructor = "রাহাত স্যার",
                            typeLabel = "লাইভ ক্লাস",
                            type = RoutineItemType.LIVE_CLASS,
                            subjectColorHex = 0xFF7C3AED,
                            isLiveNow = true
                        )
                    )
                    else -> emptyList()
                }

                else -> when (index) {
                    0 -> listOf(
                        RoutineClassItem(
                            id = "hsc_c_01",
                            time = "সন্ধ্যা ০৬:০০",
                            durationText = "১ ঘণ্টা ১৫ মিনিট",
                            subject = "বাংলা ১ম পত্র",
                            chapterOrTopic = "অপরিচিতা - রবীন্দ্রনাথ ঠাকুর (জ্ঞান ও অনুধাবন)",
                            instructor = "হাসান জামিল",
                            typeLabel = "লাইভ ক্লাস",
                            type = RoutineItemType.LIVE_CLASS,
                            subjectColorHex = 0xFFE11D48,
                            isLiveNow = false
                        )
                    )
                    4 -> listOf(
                        RoutineClassItem(
                            id = "hsc_c_02",
                            time = "রাত ০৮:০০",
                            durationText = "১ ঘণ্টা ২০ মিনিট",
                            subject = "তথ্য ও যোগাযোগ প্রযুক্তি",
                            chapterOrTopic = "ওয়েব ডিজাইন পরিচিতি এবং HTML",
                            instructor = "আসিফ আহমেদ",
                            typeLabel = "লাইভ ক্লাস",
                            type = RoutineItemType.LIVE_CLASS,
                            subjectColorHex = 0xFF0284C7,
                            isLiveNow = true
                        )
                    )
                    else -> emptyList()
                }
            }

            val exams = when (index) {
                1 -> listOf(
                    RoutineExamItem(
                        id = "ex_01",
                        time = "রাত ০৯:০০",
                        title = "সাপ্তাহিক মূল্যায়ন পরীক্ষা",
                        syllabus = "পূর্ববর্তী অধ্যায়সমূহের ওপর ২০টি MCQ",
                        marks = 20,
                        durationText = "২৫ মিনিট"
                    )
                )
                else -> emptyList()
            }

            DaySchedule(
                dayIndex = index,
                dayNameBn = fullName,
                dayShortBn = shortName,
                dateText = dates.getOrElse(index) { "০১" },
                fullDateBn = "${fullName}, ${dates.getOrElse(index) { "০১" }}/০৯/২০২৬",
                classes = classes,
                exams = exams
            )
        }
    }
}
