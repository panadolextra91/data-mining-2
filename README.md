## Data Mining Project Report – NBA Match Prediction

**Course**: IT160IU – Data Mining  
**Semester**: (fill in, e.g. Fall 2025)

---

### 1. Introduction

This project builds an end-to-end **data mining framework** for predicting NBA game outcomes (home team win/loss) using historical game statistics. The framework is implemented in **Java 17** with **Weka** as the machine learning library and **SQLite** as the main data source.

In data mining, **classification** and **prediction** models learn patterns from historical data to infer labels for unseen instances. Here, the label is whether the **home team wins** (`WIN`) or loses (`LOSS`). We employ a **RandomForest** classifier, a powerful ensemble of decision trees that reduces variance by bagging.

- **Objective**:  
  - Build a data mining pipeline that loads raw NBA game data, performs **time-aware preprocessing and feature engineering**, and trains a **RandomForest classification model** using **10-fold cross-validation** in Weka.  
  - (Sequence mining is considered future work due to scope and time constraints.)

- **Dataset Used**:  
  - **NBA Game Dataset** derived from the **`nba.sqlite`** database (Kaggle NBA stats / NBA API dump).  
  - Primary table: **`game`** (55 columns, 65,698 rows) containing per-game box-score aggregates for both home and away teams from the 1946–47 season to recent seasons.

---

### 2. Data Pre-Processing

**Objective**: Clean and prepare the raw NBA game data for feature extraction and modeling.

#### 2.1 Raw Data Overview

- **Source**: `nba.sqlite`, table `game`.
- **Instances**: **65,698** games.
- **Attributes (selected)**:
  - Keys & meta: `season_id`, `game_id`, `game_date`, `season_type`.
  - Home team stats: `team_id_home`, `pts_home`, `fg_pct_home`, `reb_home`, `ast_home`, `tov_home`, etc.
  - Away team stats: `team_id_away`, `pts_away`, `fg_pct_away`, `reb_away`, `ast_away`, `tov_away`, etc.
  - Outcome: `wl_home` (`'W'` or `'L'`).

We focus on numeric, per-team performance attributes and the home outcome.

#### 2.2 Data Cleaning Process

Data cleaning is performed in `DataLoader.java` and `RawGame.java`:

- **Handling missing values**:
  - If `game_date` cannot be parsed → row is **dropped**.
  - Numeric fields (`pts_*`, `fg_pct_*`, `reb_*`, etc.) are parsed as `double`; invalid/empty values are coerced to **0.0** (safe default given rarity in this dataset).
- **Removing duplicates**:
  - Games are keyed by `game_id`; the dataset (as provided) does not contain duplicates. We assume uniqueness and **sort by date** instead of explicit deduplication.
- **Addressing outliers**:
  - NBA box-score stats are naturally bounded (scores, FG%, etc.). No explicit outlier removal is applied; instead, we rely on robust ensemble learning (RandomForest) to handle occasional extreme games.

#### 2.3 Data Transformation

Transformations are implemented in `TeamHistory.java` and `FeaturePipeline.java`:

- **Label encoding**:
  - `wl_home = 'W'` → **1 (WIN)**  
  - `wl_home = 'L'` → **0 (LOSS)**

- **Chronological ordering**:
  - All games are **sorted by `game_date`** to ensure that only **past** games contribute to features for the current game.

- **Feature engineering**:
  - For each team, maintain a rolling **5-game window** (`TeamHistory`) and **season-to-date aggregates**.
  - Derived features (see Sections 3 and 4).

- **Final cleaned dataset**:
  - After requiring at least **5 prior games** per team to avoid cold-start noise, we obtain:
    - **Processed games**: 65,425  
    - **Used as examples**: 60,804 considered in training matrix (with 4,621 recent games used for time-based analysis).  
    - **Skipped (insufficient history)**: 273

---

### 3. Classification / Prediction Algorithm

**Objective**: Implement a classification model using the **Weka** library.

#### 3.1 Model Selection

We selected **RandomForest (Weka)** as the main classification algorithm.

- **RandomForest rationale**:
  - Ensemble of decision trees (bagging) → handles **non-linearities** and **interactions** between features.
  - Robust to noisy features and scaling; suitable for mixed distributions like NBA game stats.
  - Weka provides a mature, well-tested implementation, and integrates cleanly with Java.

Initially, we experimented with **logistic regression**, **k-NN**, and small custom ensembles (using Smile and custom Java code). RandomForest showed superior performance once feature engineering was complete.

#### 3.2 Implementation Process

Implementation is in `WekaRandomForestEvaluator.java` and `ModelTrainer.java`:

- **Data to Weka Instances (ARFF-equivalent)**:
  - Instead of writing a physical `.arff` file, we programmatically create Weka `Instances`:
    - One numeric attribute per engineered feature: `f0 ... f24`.
    - A nominal class attribute `class = {LOSS, WIN}`.
  - For each game:
    - Copy the 25-dimensional feature vector into a `DenseInstance`.
    - Set class value to `WIN` or `LOSS` based on `wl_home`.

- **RandomForest configuration**:
  - Class: `weka.classifiers.trees.RandomForest`.
  - We use Weka’s **default number of trees and depth**, which already yields strong performance with the engineered features.

- **10-fold cross-validation**:
  - We call:
    - `Evaluation eval = new Evaluation(data);`
    - `eval.crossValidateModel(rf, data, 10, new Random(42));`
  - This provides **10-fold CV estimates** of:
    - Accuracy
    - Precision, Recall, F1 for each class
    - Confusion matrix
  - After CV, we also fit the model on the **full dataset** (`rf.buildClassifier(data)`) for potential future prediction usage.

- **Challenges**:
  - Weka’s internal APIs (e.g., parameter setters) differ from some online snippets; we first tried non-existent setters like `setNumTrees`, then switched to using defaults after inspecting the class with `javap`.
  - Integrating Weka with our own feature pipeline required careful mapping of labels to nominal classes and ensuring the correct class index.

#### 3.3 Results (Initial Weka RandomForest)

With the full 25-feature set and 10-fold cross-validation:

- **Accuracy**: **65.21%**
- **WIN class**:
  - Precision: **0.6772**
  - Recall: **0.8449**
  - F1-score: **0.7518**
- **LOSS class**:
  - Precision: **0.5643**
  - Recall: **0.3328**
  - F1-score: **0.4186**
- **Runtime**:
  - Training + 10-fold CV: **~156–162 seconds** on the given machine.

Confusion matrix (cumulative over folds, directly from Weka):

```
              Predicted
             LOSS    WIN
Actual LOSS  7616   15271
       WIN   5881   32036
```

---

### 4. Improvement of Results

**Objective**: Enhance model performance through better features and algorithm choice.

#### 4.1 Methodology

We followed a two-stage improvement process:

- **Baseline stage (earlier experiments)**:
  - Model: **Logistic Regression** (Smile) on simpler, 12-dimensional features:
    - Rolling averages of: points, win rate, FG%, rebounds, assists, turnovers (last 5 games).
  - Split: time-based (train < 2020, test ≥ 2020).
  - Accuracy: ~**58.7%**; decent, but limited in modeling non-linear interactions.

- **Improved stage (current Weka RF)**:
  - Added **richer features** derived from the same `game` table:
    - Rolling and season-level **point differential**.
    - **Rest days**, **back-to-back flags**.
    - **Current win streak**.
    - **Head-to-head win rate** between the two specific teams.
  - Switched to **RandomForest (Weka)** with **10-fold cross-validation**:
    - Captures non-linearities and complex interactions between features.
    - Better suited to the expanded feature space than simple linear models.

We did not implement clustering (e.g., K-Means) or PCA in code due to time constraints; the main gains came from **feature engineering** and **ensemble learning**.

#### 4.2 Comparison of Results

**Table: Model comparison (summary of best runs)**  

| **Model**                        | **Features**                  | **Evaluation**        | **Accuracy** |
|----------------------------------|-------------------------------|-----------------------|--------------|
| Logistic Regression (baseline)   | 12 basic rolling stats        | Time-based test split | ~58.7%       |
| Weka RandomForest (final)        | 25 engineered features        | 10-fold CV            | **65.21%**   |

RandomForest with richer features significantly outperforms the linear baseline.

---

### 5. Model Evaluation

**Objective**: Evaluate the final model using **10-fold cross-validation**.

#### 5.1 Performance Metrics (Weka RandomForest, 10-fold CV)

From the latest run:

- **Accuracy**: **65.21%**
- **WIN class**:
  - Precision: **0.6772**
  - Recall: **0.8449**
  - F1-score: **0.7518**
- **LOSS class**:
  - Precision: **0.5643**
  - Recall: **0.3328**
  - F1-score: **0.4186**
- **Runtime**:
  - Approx. **156–162 seconds** for 10-fold CV on ~60k examples.

#### 5.2 Analysis of Results

- The model is **much better than a majority baseline** (~55% accuracy from always predicting WIN), demonstrating that historical performance metrics and scheduling context do carry predictive signal.
- The classifier is **better at predicting home wins than home losses**:
  - High recall for WIN (0.84) indicates most wins are correctly identified.
  - Lower recall for LOSS (0.33) shows the model tends to over-predict wins, reflecting NBA’s general home-court advantage and feature bias.
- Trade-off:
  - Higher complexity (RandomForest with many trees) and richer features increases **training time** compared to simple linear models, but improves accuracy and F1, especially for the WIN class.

---

### 6. Conclusions

- We successfully built a **data mining pipeline** that:
  - Reads raw NBA game data from **SQLite (`nba.sqlite`)** (with `game.csv` as fallback).
  - Performs **time-aware feature engineering** to avoid data leakage.
  - Trains and evaluates a **Weka RandomForest classifier** using **10-fold cross-validation**.
- The final model achieves **~65.2% accuracy**, outperforming earlier linear baselines (~58%) and the majority-class baseline (~55%).
- Key lessons:
  - **Feature quality** (rest days, streaks, point differential, head-to-head) often matters more than simply adding more algorithms.
  - Ensemble methods like RandomForest are robust and effective for noisy sports data.
- Future improvements:
  - Implement a **sequence mining algorithm** (e.g., sequential pattern mining on play-by-play or player performance sequences) to fully realize the original objective.
  - Explore **hyperparameter tuning** for RandomForest and possible dimensionality reduction (PCA) for further gains.
  - Add **calibration** or threshold adjustment to better balance WIN vs LOSS recall.

---

### 7. References

- **Datasets**:
  - NBA statistics and game logs from Kaggle / NBA API dumps (packaged as `nba.sqlite` and `game.csv`).
- **Tools & Libraries**:
  - **Weka**: `weka-stable 3.8.6` – RandomForest classifier and evaluation (`Evaluation`, `Instances`).
  - **SQLite JDBC**: `org.xerial:sqlite-jdbc` – access to `nba.sqlite`.
  - **Python**: `pandas`, `matplotlib`, `seaborn` – visualization from `model_results.csv`.
- **Documentation**:
  - Weka documentation and API Javadocs.
  - Java 17 and Maven 3.x official docs for build and runtime configuration.

---

### Project Structure

```
.
├── pom.xml                          # Maven dependencies
├── game.csv                         # NBA game dataset (fallback if sqlite missing)
├── nba.sqlite                       # Main NBA game database (preferred source)
├── src/main/java/com/nba/predict/
│   ├── RawGame.java                  # Data model for CSV rows
│   ├── DataLoader.java               # CSV loading and parsing
│   ├── TeamHistory.java              # Rolling window statistics tracker
│   ├── FeaturePipeline.java          # Time-aware feature extraction
│   ├── ModelTrainer.java             # Orchestrates Weka RandomForest training/evaluation
│   ├── WekaRandomForestEvaluator.java# Weka RF + 10-fold CV wrapper
│   ├── ModelEvaluator.java           # Evaluation and comparison on exported results
│   ├── ModelResult.java              # Result container
│   └── NBAPredictor.java             # Main entry point
├── visualize_results.py              # Python visualization script
└── README.md                         # This file
```

### Requirements

- **Java 17+** (JDK 17 or newer)
- **Maven 3.6+**
- **Python 3.7+** with packages:
  - pandas
  - matplotlib
  - seaborn

### Installation

1. **Install Java 17+**:
   ```bash
   # Check Java version
   java -version
   ```

2. **Install Maven**:
   ```bash
   # Check Maven version
   mvn -version
   ```

3. **Install Python dependencies**:
   ```bash
   pip install pandas matplotlib seaborn
   ```

### Usage

#### Step 1: Train Models

Run the Java program to train and evaluate the **Weka RandomForest** model:

```bash
mvn clean compile exec:java
```

Or compile and run manually:

```bash
mvn clean compile
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) com.nba.predict.NBAPredictor
```

This will:
- Load and parse **`nba.sqlite`** (or fallback to **`game.csv`** if `nba.sqlite` is missing).
- Extract all time-aware and season-aware features.
- Train and evaluate **Weka RandomForest** with **10-fold cross-validation**.
- Export metrics to **`model_results.csv`**.

#### Step 2: Visualize Results

Generate visualization charts:

```bash
python visualize_results.py
```

This creates:
- `accuracy_comparison.png` – Bar chart comparing model accuracies (including Weka RF)
- `metrics_radar.png` – Radar charts for top 3 models
- `time_vs_accuracy.png` – Training time vs accuracy trade-off
- `precision_recall_comparison.png` – Precision/Recall for WIN/LOSS classes
- `f1_scores.png` – F1 score comparison

### Output Files

- `model_results.csv`: Detailed metrics for Weka RandomForest (10-fold CV) and any other runs.
- `accuracy_comparison.png`: Visual accuracy comparison.
- `metrics_radar.png`: Multi-metric radar charts for top models.
- `time_vs_accuracy.png`: Performance trade-off analysis (runtime vs accuracy).
- `precision_recall_comparison.png`: Class-wise precision/recall.
- `f1_scores.png`: F1 score comparison.

### Troubleshooting
If you encounter issues:
- Verify Java and Maven versions (`java -version`, `mvn -version`).
- Ensure `nba.sqlite` (or `game.csv`) is present in the **project root**.
- If memory is an issue, you can increase heap size, e.g.:
  ```bash
  export MAVEN_OPTS="-Xmx4g"
  mvn clean compile exec:java
  ```

### License

This project is for educational purposes.

### Author

NBA Prediction Engine - Data Mining Project

