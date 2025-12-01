package com.example.setaside.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.setaside.data.model.Product

@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Product Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF618264)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                }

                // Product Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFFD0E7D2), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.imageUrl != null) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🛍️", fontSize = 64.sp)
                    }
                }

                // Product Name
                Text(
                    text = product.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Price
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    fontSize = 18.sp,
                    color = Color(0xFF618264),
                    fontWeight = FontWeight.SemiBold
                )

                // Category
                if (product.category.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Category:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Surface(
                            color = Color(0xFFD0E7D2),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 12.sp,
                                color = Color(0xFF618264),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Description
                if (!product.description.isNullOrEmpty()) {
                    Text(
                        text = product.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                }

                // Availability
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Status:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = if (product.isAvailable) "In Stock" else "Out of Stock",
                        fontSize = 14.sp,
                        color = if (product.isAvailable) Color(0xFF4CAF50) else Color.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Quantity Selector
                if (product.isAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Quantity:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFD0E7D2),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { if (quantity > 1) quantity-- }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "-",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF618264)
                                    )
                                }
                            }
                            
                            Text(
                                text = quantity.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(30.dp),
                                color = Color.Black
                            )
                            
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF618264),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { quantity++ }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = "$${String.format("%.2f", product.price * quantity)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF618264)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add to Cart Button
                Button(
                    onClick = { 
                        onAddToCart(quantity)
                        onDismiss()
                    },
                    enabled = product.isAvailable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF618264),
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (product.isAvailable) "ADD TO CART" else "OUT OF STOCK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
