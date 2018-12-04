**Run your distributed computing tasks on a variety of systems through abstraction with Xenon**

At the Netherlands eScience Center, we come across many projects that make use of remote resources, both for storage and for compute. 

Such applications typically look more or less like this: First, some data files and software files are transferred to the remote resource, for example using protocols such as SSH and SFTP. Second, the program is scheduled for execution by submitting it to a queue. This is because remote systems are commonly shared between many users, so a _scheduler_ such as Slurm, PBS, SGE, or Torque is required to do the resource allocation. Third, the program may need access to a file system in order to read its input and for writing its output.

When developing applications that use remote resources, dealing with all these different protocols and schedulers can be quite a nuisance. As a result, developers usually choose to implement only the necessary code for one file access protocol and for one scheduler.

While this simplifies matters for the developer, it comes at the cost of reduced flexibility with respect to the infrastructure that the distributed application can make use of. Such reduced flexibility can become a problem when you want to migrate to a different remote system, for example because that system is cheaper, or has better availability, or because you want to use data that is stored there and cannot be moved, for example because of legal issues or simply because the data volume is too large. 








Assuming you have a working [Conda](https://conda.io/docs/) distribution (if not, go [here](https://conda.io/docs/user-guide/install/index.html#)), install the ``xenon-cli`` package from the ``nlesc`` channel, as follows:

```bash
conda install --channel nlesc xenon-cli
```

Xenon is written in Java and has a gRPC extension, which means that you can use it from a range of languages such as C++, C#, Dart, Go, Java, Node.js, Objective-C, PHP, Python, and Ruby. [This tutorial](https://xenonrse2017.readthedocs.io/en/latest/) shows ``xenon-cli`` examples side by side with their Java and Python equivalents.

<!-- 

arnold wanted to do an analysis on big data
one half of the data was at X, the other at Y
the data was on-premises only? but too big to copy anyway 


Arnold's case:

2 data sets:

- 'donors'
- 'cell lines'

4 algorithms

- Manta
- DELLY
- LUMPY
- GRIDSS

3 hardwares

- UMCU (Grid Engine; development)
- DAS-5 (Slurm; development)
- Cartesius (Slurm; production)


we can now sample a complete DNA
doing so generates a lot of data
?@ arnold: how much data for one person / just the raw data
there is a lot of potential in these data, but: systematic and comprehensive analysis of these data remains challenging 
?@ arnold: why is detection and interpretation of structural variations more difficult than other problems?

(assert: detection and interpretation of structural variations in cancer genomes is important)
?@arnold: why cancer genomes

how does detection and interpretation of structural variations work

?@arnold how do you define a sv caller?

types of analysis that help detect and interpret structural variations
- split read
- discordantly aligned read pairs
- discordantly aligned read depth
- local short read assembly
    - sensitivity and specificity of resulting SV call sets (?@ arnold no idea what that means)

workflows are able to combine tools written in different languages.
we used snakemake to tie the components together. snakemake includes support for
distributed computing, which allows applications to run in parallel using
multiple compute nodes.

workflow to do <something important> using 4 reference tools.
each tool requires its own environment.
we solved the environments problem using conda
we wrote down the steps of the workflow in a snakemake recipe
conda gives us the environment, snakemake ties everything together in the right order

we wanted to run the workflow on two datasets: bla and bla
what made this challenging was that the data could not be moved due to privacy reasons.
so needed to do the analysis using on-premises compute infrastructure
problem: one was slurm the other sge

(maybe sidenote: this is a manifestation of larger problem, namely difficulty of reproducibility when it comes to distributed computing)

snakemake has support for slurm (?) but not SGE.

we solved the distributed-computing problem using xenon-cli

-->




