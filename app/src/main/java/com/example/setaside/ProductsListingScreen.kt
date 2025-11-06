package com.example.setaside

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource


@Composable
fun ProductsListingScreen(userName: String = "John Doe") {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 77.dp)
        ) {
            // Header - Frame 14
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF618264))
                    .padding(top = 40.dp, start = 25.dp, end = 25.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User greeting - Frame 15
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Hello,",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = userName,
                        fontSize = 30.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Cart icon with notification badge
                Box(
                    modifier = Modifier.size(38.dp)
                ) {
                    IconButton(
                        onClick = { /* Navigate to cart */ },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color.Black.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(5.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Notification badge
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = Color(0xFFFF993B),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                    )
                }
            }

            // Search bar and filter - Frame 25
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(horizontal = 28.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search bar - Frame 11
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            color = Color(0xFFD0E7D2).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Search products...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Filter button
                IconButton(
                    onClick = { /* Open filter */ },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color(0xFF618264),
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Filter",
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            // Categories Section - Frame 16
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                // Categories Header - Frame 17
                Text(
                    text = "Categories",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 25.dp, top = 10.dp, bottom = 10.dp)
                )

                // Categories Grid - Frame 18
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // First Row - Frame 20
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            name = "Veggies",
                            iconRes = R.drawable.icon_veggies,
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            name = "Meat",
                            iconRes = R.drawable.icon_meat,
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            name = "Snacks",
                            iconRes = R.drawable.icon_snacks,
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            name = "21+\nProducts",
                            iconRes = R.drawable.icon_products21,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Second Row - Frame 21
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            name = "Hygiene\nProducts",
                            iconRes = R.drawable.icon_hygiene,
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            name = "Homecare",
                            iconRes = R.drawable.icon_homecare,
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            name = "Electronics",
                            iconRes = R.drawable.icon_electronics,
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            name = "Others",
                            iconRes = R.drawable.icon_others,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Most Searched Section - Frame 26
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 10.dp)
            ) {
                // Section Header - Frame 17
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Most Searched",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Show All",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Products Grid - Frame 33
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // First Row - Frame 27
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProductCard(
                            productName = "Fresh Cabbage",
                            price = "$5.00/kg",
                            storeLocation = "Mie Bangka Ahsoka",
                            modifier = Modifier.weight(1f)
                        )
                        ProductCard(
                            productName = "Fresh Cabbage",
                            price = "$5.00/kg",
                            storeLocation = "Mie Bangka Ahsoka",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Second Row - Frame 28
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProductCard(
                            productName = "Fresh Cabbage",
                            price = "$5.00/kg",
                            storeLocation = "Mie Bangka Ahsoka",
                            modifier = Modifier.weight(1f)
                        )
                        ProductCard(
                            productName = "Fresh Cabbage",
                            price = "$5.00/kg",
                            storeLocation = "Mie Bangka Ahsoka",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Third Row - Frame 29
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProductCard(
                            productName = "Fresh Cabbage",
                            price = "$5.00/kg",
                            storeLocation = "Mie Bangka Ahsoka",
                            modifier = Modifier.weight(1f)
                        )
                        ProductCard(
                            productName = "Fresh Cabbage",
                            price = "$5.00/kg",
                            storeLocation = "Mie Bangka Ahsoka",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Bottom Navigation Bar
        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = 0
        )
    }
}

// ---------- REUSABLE PRODUCT CARD ----------
@Composable
fun ProductCard(
    productName: String,
    price: String,
    storeLocation: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(200.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 0.5.dp,
                color = Color(0xFFC6C6C6),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Product Image - Frame 29
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .background(
                    color = Color(0xFFD0E7D2),
                    shape = RoundedCornerShape(2.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for product image
            Text(
                text = "🥬",
                fontSize = 48.sp
            )
        }

        // Product Details - Frame 30
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Product Info - Frame 31
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = productName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = price,
                    fontSize = 8.sp,
                    color = Color.Gray
                )
                // Store Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "📍",
                        fontSize = 8.sp
                    )
                    Text(
                        text = storeLocation,
                        fontSize = 8.sp,
                        color = Color(0xFF618264)
                    )
                }
            }

            // Add to Cart Button - Frame 32
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        color = Color(0xFFD0E7D2),
                        shape = RoundedCornerShape(2.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ADD TO CART",
                    fontSize = 8.sp,
                    color = Color.Black
                )
            }
        }
    }
}

// ---------- BOTTOM NAVIGATION BAR ----------

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(73.dp)
    ) {
        // Top shadow gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF618264).copy(alpha = 0.15f),
                            Color(0xFF618264).copy(alpha = 0.35f),
                            Color(0xFF618264).copy(alpha = 0.5f)
                        )
                    )
                )
        )


        // Main navigation bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp)
                .background(Color.White)
                .padding(horizontal = 25.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Navigation Icons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

// Home Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (selectedTab == 0) Color(0xFF618264) else Color.Transparent,
                            shape = RoundedCornerShape(19.dp)
                        )
                        .clickable { onTabSelected(0) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Home",
                        tint = if (selectedTab == 0) Color.White else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Map Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onTabSelected(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn, // or Icons.Outlined.Map
                        contentDescription = "Map",
                        tint = if (selectedTab == 1) Color(0xFF618264) else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Orders/Bill Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onTabSelected(2) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Receipt,
                        contentDescription = "Orders",
                        tint = if (selectedTab == 2) Color(0xFF618264) else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Account Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onTabSelected(3) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Account",
                        tint = if (selectedTab == 3) Color(0xFF618264) else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}



// ---------- REUSABLE CATEGORY CARD ----------
@Composable
fun CategoryCard(
    name: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(84.dp)
            .background(
                color = Color(0xFFD0E7D2),
                shape = RoundedCornerShape(15.dp)
            )
//            .padding(top = 10.dp),
////        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {Column(
        modifier = Modifier.fillMaxWidth()
            .padding(all = 10.dp),
        horizontalAlignment = Alignment.Start
    ) { // Category Name
        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            lineHeight = 10.sp
        )}

    Column(
        modifier = Modifier.fillMaxWidth()
            .weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = name,
            modifier = Modifier.size(width = 78.dp, height = 54.dp)

        )
    }


    }
}


@Preview(showBackground = true, device = "spec:width=393dp,height=851dp")
@Composable
fun ProductsListingScreenPreview() {
    ProductsListingScreen(userName = "John Doe")
}