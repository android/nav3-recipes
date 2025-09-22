/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nav3recipes.content

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nav3recipes.ui.theme.Nav3RecipesTheme
import com.example.nav3recipes.ui.theme.PastelBlue
import com.example.nav3recipes.ui.theme.PastelGreen
import com.example.nav3recipes.ui.theme.PastelMauve
import com.example.nav3recipes.ui.theme.PastelOrange
import com.example.nav3recipes.ui.theme.PastelPink
import com.example.nav3recipes.ui.theme.PastelPurple
import com.example.nav3recipes.ui.theme.PastelRed
import com.example.nav3recipes.ui.theme.PastelYellow


@Composable
fun ContentA(onNext: () -> Unit) = ContentBase(
    title = "Content A Title",
    modifier = Modifier.background(PastelRed),
    onNext = onNext,
) {
    Text(
        modifier =
            Modifier.padding(16.dp),
        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend dui non orci eleifend bibendum. Nulla varius ultricies dolor sit amet semper. Sed accumsan, dolor id finibus rhoncus, nisi nibh suscipit augue, vitae gravida dui justo et ex. Maecenas eget suscipit lacus. Mauris ac rhoncus lacus. Suspendisse placerat eleifend magna at ornare. Duis efficitur euismod felis, vel porttitor eros hendrerit nec."
    )
}

@Composable
fun ContentB() = ContentBase(
    title = "Content B Title",
    modifier = Modifier.background(PastelGreen)
) {
    Text(
        modifier =
            Modifier.padding(16.dp),
        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend dui non orci eleifend bibendum. Nulla varius ultricies dolor sit amet semper. Sed accumsan, dolor id finibus rhoncus, nisi nibh suscipit augue, vitae gravida dui justo et ex. Maecenas eget suscipit lacus. Mauris ac rhoncus lacus. Suspendisse placerat eleifend magna at ornare. Duis efficitur euismod felis, vel porttitor eros hendrerit nec."
    )
}

@Composable
fun SampleContent(title: String, backgroundColor: Color, onNext: (() -> Unit)? = null) =
    ContentBase(
        title = title,
        modifier = Modifier.background(backgroundColor),
        onNext = onNext,
    ) {
        Text(
            modifier =
                Modifier.padding(16.dp),
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed eleifend dui non orci eleifend bibendum. Nulla varius ultricies dolor sit amet semper. Sed accumsan, dolor id finibus rhoncus, nisi nibh suscipit augue, vitae gravida dui justo et ex. Maecenas eget suscipit lacus. Mauris ac rhoncus lacus. Suspendisse placerat eleifend magna at ornare. Duis efficitur euismod felis, vel porttitor eros hendrerit nec."
        )
    }


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ContentBase(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .clip(RoundedCornerShape(48.dp))
    ) {
        Title(title)
        if (content != null) content()
        if (onNext != null) {
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = onNext
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun ColumnScope.Title(title: String) {
    Text(
        modifier = Modifier
            .padding(24.dp)
            .align(Alignment.CenterHorizontally),
        fontWeight = FontWeight.Bold,
        text = title
    )
}

@Composable
@Preview
fun ContentAPreview() {
    Nav3RecipesTheme {
        ContentA({})
    }
}

@Composable
fun Count(name: String) {
    var count: Int by rememberSaveable { mutableIntStateOf(0) }
    Column {
        Text("Count $name is $count")
        Button(onClick = { count++ }) {
            Text("Increment")
        }
    }
}


@Composable
fun ContentRed(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(
    title = title,
    modifier = modifier.background(PastelRed),
    onNext = onNext,
    content = content
)

@Composable
fun ContentOrange(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(
    title = title,
    modifier = modifier.background(PastelOrange),
    onNext = onNext,
    content = content
)

@Composable
fun ContentYellow(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(
    title = title,
    modifier = modifier.background(PastelYellow),
    onNext = onNext,
    content = content
)

@Composable
fun ContentGreen(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(
    title = title,
    modifier = modifier.background(PastelGreen),
    onNext = onNext,
    content = content
)

@Composable
fun ContentBlue(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(
    title = title,
    modifier = modifier.background(PastelBlue),
    onNext = onNext,
    content = content
)

@Composable
fun ContentMauve(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(
    title = title,
    modifier = modifier.background(PastelMauve),
    onNext = onNext,
    content = content
)

@Composable
fun ContentPurple(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(
    title = title,
    modifier = modifier.background(PastelPurple),
    onNext = onNext,
    content = content
)

@Composable
fun ContentPink(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(
    title = title,
    modifier = modifier.background(PastelPink),
    onNext = onNext,
    content = content
)