Portable distributed-computing workflows through abstraction with Xenon
Make your distributed-computing Snakemake recipes portable with Xenon
Make your distributed-computing workflows portable with Xenon
Portable workflows through abstraction with Xenon
Portable HPC workflows with Snakemake and Xenon

workflow to do <something important> using 4 reference tools.
each tool requires its own dependencies/environment.
we solved the environments problem using conda
we wrote down the steps of the workflow in a snakemake recipe
conda gives us the environment (either as virtual environment or through the use of containers)
snakemake ties everything together in the right order

we wanted to run the workflow on several data sets
what made this challenging was that the data could not be moved due to privacy reasons.
so needed to do the analysis using on-premises compute infrastructure
problem: the development compute infrastructures differs from production: SLURM vs. GE clusters

two ways forward: 
1. DRMAA (why do we not want this?)
2. scheduler-specific commands with conda's cluster option
new, 3rd way
we solved the distributed-computing problem using xenon-cli

?@arnikz: can we show a side-by-side diff of the workflow with changes highlighted?

