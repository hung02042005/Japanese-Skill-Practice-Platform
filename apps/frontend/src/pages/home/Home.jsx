import { useEffect } from 'react';
import TopBar from './sections/TopBar';
import HeroSection from './sections/HeroSection';
import FeatureASection from './sections/FeatureASection';
import FeatureBSection from './sections/FeatureBSection';
import FooterSection from './sections/FooterSection';
import './Home.css';

function Home() {
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12 }
    );

    document.querySelectorAll('.reveal').forEach((el) => observer.observe(el));

    return () => observer.disconnect();
  }, []);

  return (
    <div className="home-root">
      <TopBar />
      <main>
        <HeroSection />
        <FeatureASection />
        <FeatureBSection />
      </main>
      <FooterSection />
    </div>
  );
}

export default Home;
