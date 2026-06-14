import './RuleTag.css';

const SHORT_NAMES = {
  LargeAmountRule: 'LargeAmount',
  HighRiskCountryRule: 'HighRiskCountry',
  StructuringRule: 'Structuring',
  VelocityRule: 'Velocity',
  OriginCountryRule: 'OriginCountry',
  DormantAccountRule: 'DormantAccount',
  RoundTripRule: 'RoundTrip',
};

function RuleTag({ name, score }) {
  const shortName = SHORT_NAMES[name] || name;
  return (
    <span className="rule-tag">
      {shortName} +{score}
    </span>
  );
}

export default RuleTag;
