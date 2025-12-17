#!/usr/bin/env python3
"""
NBA Model Comparison Visualization
Creates visualizations comparing different ML models for NBA game prediction.
"""

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import sys
import os

# Set style
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (14, 8)

def load_results(csv_path='model_results.csv'):
    """Load model results from CSV."""
    if not os.path.exists(csv_path):
        print(f"Error: {csv_path} not found. Please run the Java program first.")
        sys.exit(1)
    
    df = pd.read_csv(csv_path)
    return df

def plot_confusion_matrices(df):
    """Plot confusion matrices for each model (if confusion columns exist)."""
    required = {"CM00", "CM01", "CM10", "CM11"}
    if not required.issubset(df.columns):
        print("Confusion matrix columns not found in CSV; skipping confusion plots.")
        return

    for _, row in df.iterrows():
        cm = [
            [row["CM00"], row["CM01"]],
            [row["CM10"], row["CM11"]]
        ]
        fig, ax = plt.subplots(figsize=(4, 4))
        sns.heatmap(cm, annot=True, fmt='g', cmap='Blues', cbar=False,
                    xticklabels=['Pred LOSS', 'Pred WIN'],
                    yticklabels=['Actual LOSS', 'Actual WIN'],
                    ax=ax)
        ax.set_title(f"Confusion Matrix: {row['Model_Name']}", fontsize=12, fontweight='bold')
        plt.tight_layout()

        # Safe filename
        fname = f"cm_{row['Model_Name'].replace(' ', '_').replace('(', '').replace(')', '')}.png"
        plt.savefig(fname, dpi=150)
        print(f"Saved: {fname}")
        plt.close()

def create_summary_table(df):
    """Create a formatted summary table."""
    print("\n" + "="*80)
    print("MODEL PERFORMANCE SUMMARY")
    print("="*80)
    
    df_display = df[['Model_Name', 'Accuracy', 'Training_Time_ms', 
                     'F1_WIN', 'F1_LOSS']].copy()
    df_display['Accuracy'] = (df_display['Accuracy'] * 100).round(2)
    df_display['F1_WIN'] = df_display['F1_WIN'].round(4)
    df_display['F1_LOSS'] = df_display['F1_LOSS'].round(4)
    df_display = df_display.sort_values('Accuracy', ascending=False)
    
    print(df_display.to_string(index=False))
    print("="*80)
    
    # Best model
    best = df.loc[df['Accuracy'].idxmax()]
    print(f"\n🏆 BEST MODEL: {best['Model_Name']}")
    print(f"   Accuracy: {best['Accuracy']*100:.2f}%")
    print(f"   Training Time: {best['Training_Time_ms']} ms")
    print(f"   F1 (WIN): {best['F1_WIN']:.4f}")
    print(f"   F1 (LOSS): {best['F1_LOSS']:.4f}")

def main():
    """Main visualization function."""
    csv_path = 'model_results.csv'
    if len(sys.argv) > 1:
        csv_path = sys.argv[1]
    
    print("NBA Model Comparison Visualization")
    print("="*80)
    
    df = load_results(csv_path)
    
    print(f"\nLoaded {len(df)} models from {csv_path}")
    
    # Only confusion matrices and summary table
    plot_confusion_matrices(df)
    create_summary_table(df)
    
    print("\n✅ Confusion matrix plots and summary table generated.")

if __name__ == '__main__':
    main()

