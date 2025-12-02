//package com.example.setaside
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.outlined.Close
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//
//@Composable
//fun ProductDetailDialog(
//    productName: String,
//    onDismiss: () -> Unit
//) {
//    var selectedTab by remember { mutableStateOf("Details") }
//    Dialog(onDismissRequest = onDismiss) {
//        //Pop-up Window - Frame 37
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp)
//                .background(Color.White, RoundedCornerShape(12.dp))
//        ) {
//            Column(
//                modifier = Modifier.padding(15.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                //Tabs - Frame 41
//                Box(modifier = Modifier.fillMaxWidth()) {
//                    Row(
//                        modifier = Modifier.align(Alignment.CenterStart),
//                        horizontalArrangement = Arrangement.spacedBy(15.dp)
//                    ) {
//                        // Details Tab
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            modifier = Modifier.clickable { selectedTab = "Details" }
//                        ) {
//                            Text(
//                                text = "Details",
//                                fontSize = 10.sp,
//                                fontWeight = if (selectedTab == "Details") FontWeight.Bold else FontWeight.Normal,
//                                color = if (selectedTab == "Details") Color(0xFF618264) else Color.Gray
//                            )
//                            if (selectedTab == "Details") {
//                                Box(
//                                    modifier = Modifier
//                                        .height(2.dp)
//                                        .width(40.dp)
//                                        .background(Color(0xFF618264))
//                                )
//                            }
//                        }
//
//                        // Location Tab
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            modifier = Modifier.clickable { selectedTab = "Location" }
//                        ) {
//                            Text(
//                                text = "Location",
//                                fontSize = 10.sp,
//                                fontWeight = if (selectedTab == "Location") FontWeight.Bold else FontWeight.Normal,
//                                color = if (selectedTab == "Location") Color(0xFF618264) else Color.Gray
//                            )
//                            if (selectedTab == "Location") {
//                                Box(
//                                    modifier = Modifier
//                                        .height(2.dp)
//                                        .width(40.dp)
//                                        .background(Color(0xFF618264))
//                                )
//                            }
//                        }
//                    }
//
//                    // Close Button (X)
//                    IconButton(
//                        onClick = onDismiss,
//                        modifier = Modifier
//                            .align(Alignment.CenterEnd)
//                            .size(15.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Outlined.Close,
//                            contentDescription = "Close",
//                            tint = Color.Black
//                        )
//                    }
//                }
//
//                //Product Details - Frame 38
//                when (selectedTab) {
//                    "Details" -> {
//                        Column(
//                            verticalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            // Product Image
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(160.dp)
//                                    .background(Color(0xFFD0E7D2), RoundedCornerShape(8.dp)),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text("🧻", fontSize = 48.sp)
//                            }
//
//                            // Product Name
//                            Text(productName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//
//                            // Price
//                            Text("$20/pc", fontSize = 15.sp, color = Color.Black)
//
//                            // Description
//                            Text(
//                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum",
//                                fontSize = 10.sp,
//                                color = Color.Black
//                            )
//                        }
//                    }
//
//                    "Location" -> {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 40.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Text(
//                                "📍 Store Location Coming Soon",
//                                fontSize = 14.sp,
//                                color = Color.Gray
//                            )
//                        }
//                    }
//                }
//
//                // Add to Cart Button - Frame 39
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(45.dp)
//                        .background(Color(0xFFD0E7D2), RoundedCornerShape(2.dp))
//                        .clickable { /* handle add to cart */ },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("ADD TO CART", color = Color.Black, fontWeight = FontWeight.Bold)
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ProductDetailDialogPreview() {
//    ProductDetailDialog(
//        productName = "Fresh Cabbage",
//        onDismiss = {}
//    )
//}
